package com.devnovus.oneBox.web.folder;

import com.devnovus.oneBox.domain.folder.dto.FolderMoveRequest;
import com.devnovus.oneBox.domain.folder.dto.FolderRenameRequest;
import com.devnovus.oneBox.domain.folder.service.FolderService;
import com.devnovus.oneBox.domain.metadata.entity.Metadata;
import com.devnovus.oneBox.domain.metadata.enums.MetadataType;
import com.devnovus.oneBox.domain.metadata.repository.MetadataRepository;
import com.devnovus.oneBox.domain.user.entity.User;
import com.devnovus.oneBox.domain.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("폴더 동시성 테스트")
public class FolderConcurrencyTest {
    @Autowired private FolderService folderService;
    @Autowired private UserRepository userRepository;
    @Autowired private MetadataRepository metadataRepository;
    @Autowired private PlatformTransactionManager txManager;

    private User user;
    private Metadata root;
    private Metadata parentA;
    private Metadata parentB;
    private Metadata parentC;
    private Metadata childA;
    private Metadata childC;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User());
        root = metadataRepository.save(new Metadata(user, null, "root", MetadataType.FOLDER, 0L, null, null));
        parentA = metadataRepository.save(new Metadata(user, root, "A", MetadataType.FOLDER, 0L, null, null));
        parentB = metadataRepository.save(new Metadata(user, root, "B", MetadataType.FOLDER, 0L, null, null));
        parentC = metadataRepository.save(new Metadata(user, root, "C", MetadataType.FOLDER, 0L, null, null));
        childA = metadataRepository.save(new Metadata(user, parentA, "cA", MetadataType.FOLDER, 0L, null, null));
        childC = metadataRepository.save(new Metadata(user, parentC, "cC", MetadataType.FOLDER, 0L, null, null));
        metadataRepository.flush();
    }

    @AfterEach
    void clear() {
        metadataRepository.deleteAll();
        userRepository.deleteAll();
    }

    private boolean[] runConcurrent(Callable<Boolean> taskA, Callable<Boolean> taskB) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            Future<Boolean> resultA = executor.submit(() -> {
                startLatch.await();
                return taskA.call();
            });
            Future<Boolean> resultB = executor.submit(() -> {
                startLatch.await();
                return taskB.call();
            });

            startLatch.countDown();

            boolean successA = resultA.get();
            boolean successB = resultB.get();

            return new boolean[]{ successA, successB };
        } finally {
            executor.shutdown();
        }
    }

    private boolean tryMove(Long folderId, FolderMoveRequest req) {
        try {
            folderService.moveFolder(folderId, req);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean tryRename(Long folderId, FolderRenameRequest req) {
        try {
            folderService.renameFolder(folderId, req);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Nested
    @DisplayName("폴더 이동 테스트")
    class MoveFolderValidation {

        @Test
        @DisplayName("동시에 동일 폴더를 서로 다른 부모로 이동 시 하나만 성공해야 한다")
        void concurrent_differentParents() throws Exception {
            FolderMoveRequest requestA = new FolderMoveRequest(parentA.getId());
            FolderMoveRequest requestB = new FolderMoveRequest(parentB.getId());

            runConcurrent(
                    () -> tryMove(parentC.getId(), requestA),
                    () -> tryMove(parentC.getId(), requestB)
            );
        }

        @Test
        @DisplayName("동시에 동일 경로로 같은 이름의 폴더를 이동 시 하나만 성공해야 한다")
        void concurrent_duplicateName() throws Exception {
            Metadata otherA = Metadata.builder()
                    .owner(user).parentFolder(parentC).name("A").type(MetadataType.FOLDER)
                    .build();
            metadataRepository.save(otherA);

            FolderMoveRequest requestA = new FolderMoveRequest(parentB.getId());
            FolderMoveRequest requestB = new FolderMoveRequest(parentB.getId());

            boolean[] results = runConcurrent(
                    () -> tryMove(otherA.getId(), requestA),
                    () -> tryMove(parentA.getId(), requestB)
            );

            assertThat(results[0] ^ results[1]).isTrue();
        }

        @Test
        @DisplayName("동시에 순환 이동 시도 시 순환 구조가 생성되지 않아야 한다")
        void concurrent_circularMoveAttempt() throws Exception {
            FolderMoveRequest requestA = new FolderMoveRequest(parentB.getId());
            FolderMoveRequest requestB = new FolderMoveRequest(parentA.getId());

            boolean[] results = runConcurrent(
                    () -> tryMove(parentA.getId(), requestA),
                    () -> tryMove(parentB.getId(), requestB)
            );

            assertThat(results[0] ^ results[1]).isTrue();
        }

        @Test
        @DisplayName("동시에 순환 이동 시도 시 순환 구조가 생성되지 않아야 한다 - 2")
        void concurrent_circularMoveAttempt_2() throws Exception {
            FolderMoveRequest requestA = new FolderMoveRequest(childC.getId());
            FolderMoveRequest requestB = new FolderMoveRequest(parentA.getId());

            boolean[] results = runConcurrent(
                    () -> tryMove(parentA.getId(), requestA),
                    () -> tryMove(parentC.getId(), requestB)
            );

            assertThat(results[0] ^ results[1]).isTrue();
        }

        @Test
        @DisplayName("동시에 순환 이동 시도 시 순환 구조가 생성되지 않아야 한다 - 3")
        void concurrent_circularMoveAttempt_3() throws Exception {
            FolderMoveRequest requestA = new FolderMoveRequest(childC.getId());
            FolderMoveRequest requestB = new FolderMoveRequest(childA.getId());

            boolean[] results = runConcurrent(
                    () -> tryMove(parentA.getId(), requestA),
                    () -> tryMove(parentC.getId(), requestB)
            );

            assertThat(results[0] ^ results[1]).isTrue();
        }
    }

    @Nested
    @DisplayName("폴더 이름 수정 테스트")
    class RenameFolderValidation {

        @Test
        @DisplayName("동시에 동일 이름으로 폴더명을 변경 시 하나만 성공해야 한다")
        void concurrent_duplicateName() throws Exception {
            String name = "D";
            FolderRenameRequest requestA = new FolderRenameRequest(name);
            FolderRenameRequest requestB = new FolderRenameRequest(name);

            boolean[] results = runConcurrent(
                    () -> tryRename(parentA.getId(), requestA),
                    () -> tryRename(parentB.getId(), requestB)
            );

            assertThat(results[0] ^ results[1]).isTrue();
        }

        @Test
        @DisplayName("동시에 폴더 이름 변경과 폴더 이동할 때 경로 정합성이 유지되어야 한다")
        void concurrent_renameAndMoveFolder() throws Exception {
            String newName = "D";
            Long parentAId = parentA.getId();

            FolderRenameRequest renameReq = new FolderRenameRequest(newName);
            FolderMoveRequest moveReq = new FolderMoveRequest(parentAId);

            runConcurrent(
                    () -> tryRename(parentC.getId(), renameReq),
                    () -> tryMove(parentC.getId(), moveReq)
            );
        }
    }
}
