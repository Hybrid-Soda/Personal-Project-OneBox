package com.devnovus.oneBox.web.folder;

import com.devnovus.oneBox.domain.folder.dto.MoveFolderRequest;
import com.devnovus.oneBox.domain.folder.dto.RenameFolderRequest;
import com.devnovus.oneBox.domain.folder.service.FolderServiceV1;
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
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("폴더 동시성 테스트")
public class FolderConcurrencyTest {
    @Autowired private FolderServiceV1 folderService;
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

        root = metadataRepository.save(
                Metadata.builder()
                        .owner(user).parentFolder(null).name("root").path("/").type(MetadataType.FOLDER)
                        .build()
        );
        parentA = metadataRepository.save(
                Metadata.builder()
                        .owner(user).parentFolder(root).name("A").path("/A/").type(MetadataType.FOLDER)
                        .build()
        );
        parentB = metadataRepository.save(
                Metadata.builder()
                        .owner(user).parentFolder(root).name("B").path("/B/").type(MetadataType.FOLDER)
                        .build()
        );
        parentC = metadataRepository.save(
                Metadata.builder()
                        .owner(user).parentFolder(root).name("C").path("/C/").type(MetadataType.FOLDER)
                        .build()
        );
        childA = metadataRepository.save(
                Metadata.builder()
                        .owner(user).parentFolder(parentA).name("cA").path("/A/cA/").type(MetadataType.FOLDER)
                        .build()
        );
        childC = metadataRepository.save(
                Metadata.builder()
                        .owner(user).parentFolder(parentC).name("cC").path("/C/cC/").type(MetadataType.FOLDER)
                        .build()
        );
        metadataRepository.flush();
    }

    @AfterEach
    void clear() {
        metadataRepository.deleteAll();
        userRepository.deleteAll();
    }

    /** 스레드별 독립 트랜잭션 강제 */
    private Boolean executeInTx(Callable<Boolean> task) {
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        return tx.execute(status -> {
            try {
                return task.call();
            } catch (Exception e) {
                return false;
            }
        });
    }

    private boolean[] runConcurrent(Callable<Boolean> taskA, Callable<Boolean> taskB) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Future<Boolean> resultA = executor.submit(() -> {
            startLatch.await();
            return taskA.call();
//            return executeInTx(taskA);
        });

        Future<Boolean> resultB = executor.submit(() -> {
            startLatch.await();
            return taskB.call();
//            return executeInTx(taskB);
        });

        startLatch.countDown();

        boolean successA = resultA.get();
        boolean successB = resultB.get();

        System.out.printf("successA: %b, successB: %b%n", successA, successB);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        return new boolean[]{ successA, successB };
    }

    private boolean tryMove(Long folderId, MoveFolderRequest req) {
        try {
            folderService.moveFolder(folderId, req);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean tryRename(Long folderId, RenameFolderRequest req) {
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
            MoveFolderRequest requestA = new MoveFolderRequest(user.getId(), parentA.getId());
            MoveFolderRequest requestB = new MoveFolderRequest(user.getId(), parentB.getId());

            runConcurrent(
                    () -> tryMove(parentC.getId(), requestA),
                    () -> tryMove(parentC.getId(), requestB)
            );

            Metadata updatedParentC = metadataRepository.findById(parentC.getId()).orElseThrow();
            Metadata updatedChildC = metadataRepository.findById(childC.getId()).orElseThrow();

            String expectedPathA = parentA.getPath() + parentC.getName() + "/";
            String expectedPathB = parentB.getPath() + parentC.getName() + "/";
            String expectedPathC = updatedParentC.getPath() + childC.getName() + "/";

            assertAll(
                    () -> assertThat(
                            updatedParentC.getPath().equals(expectedPathA) ||
                                    updatedParentC.getPath().equals(expectedPathB)
                    ).isTrue(),
                    () -> assertThat(updatedChildC.getPath()).isEqualTo(expectedPathC)
            );
        }

        @Test
        @DisplayName("동시에 동일 경로로 같은 이름의 폴더를 이동 시 하나만 성공해야 한다")
        void concurrent_duplicateName() throws Exception {
            Metadata otherA = Metadata.builder()
                    .owner(user).parentFolder(parentC).name("A").path("/C/A/").type(MetadataType.FOLDER)
                    .build();
            metadataRepository.save(otherA);

            MoveFolderRequest requestA = new MoveFolderRequest(user.getId(), parentB.getId());
            MoveFolderRequest requestB = new MoveFolderRequest(user.getId(), parentB.getId());

            boolean[] results = runConcurrent(
                    () -> tryMove(otherA.getId(), requestA),
                    () -> tryMove(parentA.getId(), requestB)
            );

            assertThat(results[0] ^ results[1]).isTrue();
        }

        @Test
        @DisplayName("동시에 순환 이동 시도 시 순환 구조가 생성되지 않아야 한다")
        void concurrent_circularMoveAttempt() throws Exception {
            MoveFolderRequest requestA = new MoveFolderRequest(user.getId(), parentB.getId());
            MoveFolderRequest requestB = new MoveFolderRequest(user.getId(), parentA.getId());

            boolean[] results = runConcurrent(
                    () -> tryMove(parentA.getId(), requestA),
                    () -> tryMove(parentB.getId(), requestB)
            );

            assertThat(results[0] ^ results[1]).isTrue();
        }

        @Test
        @DisplayName("동시에 순환 이동 시도 시 순환 구조가 생성되지 않아야 한다 - 2")
        void concurrent_circularMoveAttempt_2() throws Exception {
            MoveFolderRequest requestA = new MoveFolderRequest(user.getId(), childC.getId());
            MoveFolderRequest requestB = new MoveFolderRequest(user.getId(), parentA.getId());

            boolean[] results = runConcurrent(
                    () -> tryMove(parentA.getId(), requestA),
                    () -> tryMove(parentC.getId(), requestB)
            );

            assertThat(results[0] ^ results[1]).isTrue();
        }

        @Test
        @DisplayName("동시에 순환 이동 시도 시 순환 구조가 생성되지 않아야 한다 - 3")
        void concurrent_circularMoveAttempt_3() throws Exception {
            MoveFolderRequest requestA = new MoveFolderRequest(user.getId(), childC.getId());
            MoveFolderRequest requestB = new MoveFolderRequest(user.getId(), childA.getId());

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
            RenameFolderRequest requestA = new RenameFolderRequest(user.getId(), name);
            RenameFolderRequest requestB = new RenameFolderRequest(user.getId(), name);

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
            RenameFolderRequest renameReq = new RenameFolderRequest(user.getId(), newName);
            MoveFolderRequest moveReq = new MoveFolderRequest(user.getId(), parentA.getId());

            runConcurrent(
                    () -> tryRename(parentC.getId(), renameReq),
                    () -> tryMove(parentC.getId(), moveReq)
            );

            Metadata updatedParentC = metadataRepository.findById(parentC.getId()).orElseThrow();
            Metadata updatedChildC = metadataRepository.findById(childC.getId()).orElseThrow();
            String expectedChildCPath = updatedParentC.getPath() + updatedChildC.getName() + "/";

            assertAll(
                    // C는 A의 자식이어야 한다.
                    () -> assertThat(updatedParentC.getParentFolder().getId()).isEqualTo(parentA.getId()),
                    // 이름은 newName으로 변경되어야 한다.
                    () -> assertThat(updatedParentC.getName()).isEqualTo(newName),
                    // 자식 폴더(cC)의 parent는 일관되어야 한다.
                    () -> assertThat(updatedChildC.getParentFolder().getId()).isEqualTo(updatedParentC.getId()),
                    // 자식 폴더(cC)의 path는 일관되어야 한다.
                    () -> assertThat(updatedChildC.getPath()).isEqualTo(expectedChildCPath)
            );
        }
    }

    @Nested
    @DisplayName("폴더 경로 수정 테스트")
    class BulkUpdatePathValidation {

        @Test
        @DisplayName("폴더 이동 대량 경로 업데이트 중, 하위 폴더 변경 작업과 경합 시 경로가 깨지지 않아야 한다")
        void concurrent_conflictWithChildUpdate() throws Exception {
            String newChildName = "cC_renamed";
            MoveFolderRequest moveReq = new MoveFolderRequest(user.getId(), parentB.getId());
            RenameFolderRequest renameReq = new RenameFolderRequest(user.getId(), newChildName);

            runConcurrent(
                    () -> tryMove(parentC.getId(), moveReq),
                    () -> tryRename(childC.getId(), renameReq)
            );

            Metadata updatedParentC = metadataRepository.findById(parentC.getId()).orElseThrow();
            Metadata updatedChildC = metadataRepository.findById(childC.getId()).orElseThrow();
            String expectedPath = updatedParentC.getPath() + updatedChildC.getName() + "/";

            assertAll(
                    // A 폴더는 최종적으로 B 아래로 이동되어야 한다.
                    () -> assertThat(updatedParentC.getParentFolder().getId()).isEqualTo(parentB.getId()),
                    // childA 는 여전히 A 의 자식이어야 한다.
                    () -> assertThat(updatedChildC.getParentFolder().getId()).isEqualTo(updatedParentC.getId()),
                    // childA 의 이름은 요청대로 변경되어야 한다.
                    () -> assertThat(updatedChildC.getName()).isEqualTo(newChildName),
                    // path도 일관되어야 한다.
                    () -> assertThat(updatedChildC.getPath()).isEqualTo(expectedPath)
            );
        }
    }
}
