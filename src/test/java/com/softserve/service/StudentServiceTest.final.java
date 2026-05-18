package com.softserve.service;

import com.softserve.dto.GroupDTO;
import com.softserve.dto.StudentDTO;
import com.softserve.dto.StudentImportDTO;
import com.softserve.dto.enums.ImportSaveStatus;
import com.softserve.entity.Group;
import com.softserve.entity.Student;
import com.softserve.entity.User;
import com.softserve.entity.enums.Role;
import com.softserve.exception.EntityNotFoundException;
import com.softserve.exception.FieldAlreadyExistsException;
import com.softserve.exception.FieldNullException;
import com.softserve.mapper.GroupMapper;
import com.softserve.mapper.StudentMapper;
import com.softserve.repository.StudentRepository;
import com.softserve.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    private static final String TEST_STUDENTS_FILE_PATH = "src/test/resources/test_students.csv";

    @InjectMocks
    private StudentServiceImpl studentService;

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private UserService userService;
    @Mock
    private GroupService groupService;
    @Mock
    private StudentMapper studentMapper;
    @Mock
    private GroupMapper groupMapper;

    private Student studentWithId1L;
    private StudentDTO studentDTOWithId1L;
    private StudentDTO studentDTOWithId2L;

    @BeforeEach
    void setUp() {
        User userWithId1L = new User();
        userWithId1L.setId(1L);
        userWithId1L.setEmail("userWithId1L@test.com");
        userWithId1L.setPassword("12345@testAa");
        userWithId1L.setRole(Role.ROLE_STUDENT);

        studentWithId1L = new Student();
        studentWithId1L.setName("Name");
        studentWithId1L.setSurname("Surname");
        studentWithId1L.setPatronymic("Patronymic");
        studentWithId1L.setUser(userWithId1L);

        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setId(3L);
        groupDTO.setTitle("Test");

        studentDTOWithId1L = new StudentDTO();
        studentDTOWithId1L.setId(null);
        studentDTOWithId1L.setName("Name");
        studentDTOWithId1L.setSurname("Surname");
        studentDTOWithId1L.setPatronymic("Patronymic");
        studentDTOWithId1L.setEmail("aware.123db@gmail.com");
        studentDTOWithId1L.setGroup(groupDTO);

        studentDTOWithId2L = new StudentDTO();
        studentDTOWithId2L.setId(null);
        studentDTOWithId2L.setName("Name");
        studentDTOWithId2L.setSurname("Surname");
        studentDTOWithId2L.setPatronymic("Patronymic");
        studentDTOWithId2L.setEmail(null);
    }

    // ─── getAll / getById ─────────────────────────────────────────────────────

    @Nested
    class BasicCrud {

        @Test
        void getAll() {
            // Arrange
            List<Student> expected = singletonList(studentWithId1L);
            when(studentRepository.getAll()).thenReturn(expected);
            // Act
            List<Student> actual = studentService.getAll();
            // Assert
            assertThat(actual).hasSameSizeAs(expected).hasSameElementsAs(expected);
            verify(studentRepository).getAll();
        }

        @Test
        void getById() {
            // Arrange
            Student expected = studentWithId1L;
            when(studentRepository.findById(expected.getId())).thenReturn(Optional.of(expected));
            // Act
            Student actual = studentService.getById(expected.getId());
            // Assert
            assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
            verify(studentRepository).findById(expected.getId());
        }

        @Test
        void save() {
            // Arrange
            Student expected = studentWithId1L;
            when(studentRepository.save(expected)).thenReturn(expected);
            // Act
            Student actual = studentService.save(expected);
            // Assert
            assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
            verify(studentRepository).save(expected);
        }

        @Test
        void update() {
            // Arrange
            Student expected = studentWithId1L;
            when(studentRepository.update(expected)).thenReturn(expected);
            // Act
            Student actual = studentService.update(expected);
            // Assert
            assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
            verify(studentRepository).update(expected);
        }

        @Test
        void delete() {
            // Arrange
            Student expected = studentWithId1L;
            when(studentRepository.delete(expected)).thenReturn(expected);
            // Act
            Student actual = studentService.delete(expected);
            // Assert
            assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
            verify(studentRepository).delete(expected);
        }

        @Test
        void throwEntityNotFoundExceptionWhenGetById() {
            // Arrange
            when(studentRepository.findById(1L)).thenReturn(Optional.empty());
            // Act + Assert
            assertThrows(EntityNotFoundException.class, () -> studentService.getById(1L));
            verify(studentRepository).findById(1L);
        }
    }

    // ─── save(StudentDTO) ─────────────────────────────────────────────────────

    @Nested
    class SaveDTO {

        @Test
        void throwFieldAlreadyExistsExceptionWhenSave() {
            // Arrange
            User user = buildUser(16L, "aware.123db@gmail.com");
            Student student = buildStudent(null, user, buildGroup(1L, "First Title"));
            StudentDTO expected = studentDTOWithId1L;

            when(studentMapper.studentDTOToStudent(expected)).thenReturn(student);
            when(userService.findSocialUser(expected.getEmail())).thenReturn(Optional.of(user));
            when(studentRepository.isEmailInUse(anyString())).thenReturn(true);
            // Act + Assert
            assertThrows(FieldAlreadyExistsException.class, () -> studentService.save(expected));
        }

        @Test
        void throwFieldNullExceptionWhenSave() {
            // Arrange
            Student student = buildStudent(null, null, null);
            StudentDTO expected = studentDTOWithId2L;
            when(studentMapper.studentDTOToStudent(expected)).thenReturn(student);
            // Act + Assert
            assertThrows(FieldNullException.class, () -> studentService.save(expected));
        }

        @Test
        void saveStudentSuccessfullyWhenSocialUserExistsAndEmailIsFree() {
            User user = buildUser(16L, studentDTOWithId1L.getEmail());

            Student mappedStudent = buildStudent(null, null, null);

            Student savedStudent = buildStudent(
                    10L,
                    user,
                    buildGroup(1L, "First Title")
            );

            StudentDTO dto = studentDTOWithId1L;

            when(studentMapper.studentDTOToStudent(dto))
                    .thenReturn(mappedStudent);

            when(userService.findSocialUser(dto.getEmail()))
                    .thenReturn(Optional.of(user));

            when(studentRepository.isEmailInUse(dto.getEmail()))
                    .thenReturn(false);

            when(userService.automaticRegistration(
                    dto.getEmail(),
                    Role.ROLE_STUDENT
            )).thenReturn(user);

            when(studentRepository.save(any(Student.class)))
                    .thenReturn(savedStudent);

            Student result = studentService.save(dto);

            assertNotNull(result);

            assertEquals(user, result.getUser());

            verify(userService)
                    .automaticRegistration(dto.getEmail(), Role.ROLE_STUDENT);

            verify(studentRepository)
                    .save(any(Student.class));
        }
    }

    // ─── update(StudentDTO) ───────────────────────────────────────────────────

    @Nested
    class UpdateDTO {

        @Test
        void throwFieldNullExceptionWhenUpdate() {
            // Arrange
            Student student = buildStudent(null, null, null);
            StudentDTO expected = studentDTOWithId2L;
            when(studentMapper.studentDTOToStudent(expected)).thenReturn(student);
            // Act + Assert
            assertThrows(FieldNullException.class, () -> studentService.update(expected));
        }

        @Test
        void throwEntityNotFoundExceptionWhenUpdate() {
            // Arrange
            User user = buildUser(16L, "aware.123db@gmail.com");
            Student student = buildStudent(null, user, buildGroup(1L, "First Title"));
            StudentDTO expected = studentDTOWithId1L;

            when(studentMapper.studentDTOToStudent(expected)).thenReturn(student);
            when(studentRepository.isIdPresent(student.getId())).thenReturn(false);
            // Act + Assert
            assertThrows(EntityNotFoundException.class, () -> studentService.update(expected));
        }

        @Test
        void throwFieldAlreadyExistsExceptionWhenEmailBelongsToAnotherStudent() {
            // Arrange — email exists in the system but belongs to a DIFFERENT student
            User anotherUser = buildUser(99L, "aware.123db@gmail.com");
            Student student = buildStudent(5L, anotherUser, buildGroup(1L, "Test Group"));
            StudentDTO dto = studentDTOWithId1L;
            dto.setId(5L);

            when(studentMapper.studentDTOToStudent(dto)).thenReturn(student);
            when(studentRepository.isIdPresent(student.getId())).thenReturn(true);
            // findSocialUser returns a user → email exists in system
            when(userService.findSocialUser(dto.getEmail())).thenReturn(Optional.of(anotherUser));
            // but the email does NOT belong to this student
            when(studentRepository.isEmailForThisStudent(dto.getEmail(), student.getId())).thenReturn(false);
            // Act + Assert
            assertThrows(FieldAlreadyExistsException.class, () -> studentService.update(dto));
            verify(studentRepository, never()).update(any(Student.class));
        }

        @Test
        void registersNewUserWhenFindSocialUserReturnsEmpty() {
            // Arrange — findSocialUser returns empty → automaticRegistration should be triggered
            User newlyRegisteredUser = buildUser(42L, "aware.123db@gmail.com");
            Student student = buildStudent(5L, null, buildGroup(1L, "Test Group"));
            Student registeredStudent = buildStudent(5L, newlyRegisteredUser, buildGroup(1L, "Test Group"));
            StudentDTO dto = studentDTOWithId1L;
            dto.setId(5L);

            when(studentMapper.studentDTOToStudent(dto)).thenReturn(student);
            when(studentRepository.isIdPresent(student.getId())).thenReturn(true);
            when(userService.findSocialUser(dto.getEmail())).thenReturn(Optional.empty());
            // automaticRegistration is called internally via registerStudent
            when(userService.automaticRegistration(dto.getEmail(), Role.ROLE_STUDENT)).thenReturn(newlyRegisteredUser);
            when(studentRepository.update(any(Student.class))).thenReturn(registeredStudent);
            // Act
            Student result = studentService.update(dto);
            // Assert — a new user is registered and the student is updated
            assertNotNull(result);
            verify(userService).automaticRegistration(dto.getEmail(), Role.ROLE_STUDENT);
            verify(studentRepository).update(any(Student.class));
        }

        @Test
        void updateStudentSuccessfullyWhenSocialUserExistsAndEmailBelongsToThisStudent() {
            User user = buildUser(16L, studentDTOWithId1L.getEmail());

            Student student = buildStudent(
                    5L,
                    null,
                    buildGroup(1L, "Test Group")
            );

            Student updatedStudent = buildStudent(
                    5L,
                    user,
                    buildGroup(1L, "Test Group")
            );

            StudentDTO dto = studentDTOWithId1L;
            dto.setId(5L);

            when(studentMapper.studentDTOToStudent(dto))
                    .thenReturn(student);

            when(studentRepository.isIdPresent(student.getId()))
                    .thenReturn(true);

            when(userService.findSocialUser(dto.getEmail()))
                    .thenReturn(Optional.of(user));

            when(studentRepository.isEmailForThisStudent(
                    dto.getEmail(),
                    student.getId()
            )).thenReturn(true);

            when(studentRepository.update(any(Student.class)))
                    .thenReturn(updatedStudent);

            Student result = studentService.update(dto);

            assertNotNull(result);

            assertEquals(user, result.getUser());

            verify(studentRepository).update(argThat(updated ->
                    updated.getUser() != null &&
                    updated.getUser().equals(user)
            ));
        }
    }

    // ─── saveStudentFromFile / saveFromFile ───────────────────────────────────

    @Nested
    class SaveFromFile {

        static Stream<Arguments> parametersToTestImport() throws IOException {
            byte[] fileContent = Files.readAllBytes(Path.of(TEST_STUDENTS_FILE_PATH));

            MockMultipartFile multipartFileCsv = new MockMultipartFile("file",
                    "students.csv", "text/csv", fileContent);
            MockMultipartFile multipartFileTxt = new MockMultipartFile("file",
                    "students.txt", "text/plain", fileContent);

            return Stream.of(
                    Arguments.of(multipartFileCsv),
                    Arguments.of(multipartFileTxt)
            );
        }

        @ParameterizedTest
        @MethodSource("parametersToTestImport")
        void importStudentsFromFile(MockMultipartFile multipartFile) {
            // Arrange
            User userWithId1L = buildUser(1L, "romaniuk@gmail.com");
            User userWithId2L = buildUser(2L, "hanushchak@bigmir.net");
            Group group = buildGroup(10L, null);
            GroupDTO groupDTO = new GroupDTO();
            groupDTO.setId(10L);

            StudentImportDTO studentImportDTO1 = buildImportDTO("Hanna", "Romaniuk", "Stepanivna", "romaniuk@gmail.com");
            StudentImportDTO studentImportDTO2 = buildImportDTO("Oleksandr", "Boichuk", "Ivanovych", "");
            studentImportDTO2.setGroupDTO(null);
            studentImportDTO2.setImportSaveStatus(ImportSaveStatus.VALIDATION_ERROR);
            StudentImportDTO studentImportDTO3 = buildImportDTO("Viktor", "Hanushchak", "Mykolaiovych", "hanushchak@bigmir.net");

            List<StudentImportDTO> expectedStudents = new ArrayList<>();
            expectedStudents.add(studentImportDTO1);
            expectedStudents.add(studentImportDTO2);
            expectedStudents.add(studentImportDTO3);

            Student student1 = buildStudent(null, null, null);
            student1.setName("Hanna"); student1.setSurname("Romaniuk"); student1.setPatronymic("Stepanivna");
            Student student1registered = buildStudent(null, userWithId1L, group);
            student1registered.setName("Hanna"); student1registered.setSurname("Romaniuk"); student1registered.setPatronymic("Stepanivna");

            Student student3 = buildStudent(null, null, null);
            student3.setName("Viktor"); student3.setSurname("Hanushchak"); student3.setPatronymic("Mykolaiovych");
            Student student3registered = buildStudent(null, userWithId2L, group);
            student3registered.setName("Viktor"); student3registered.setSurname("Hanushchak"); student3registered.setPatronymic("Mykolaiovych");

            StudentDTO studentDTO1 = new StudentDTO();
            studentDTO1.setName("Hanna"); studentDTO1.setSurname("Romaniuk"); studentDTO1.setPatronymic("Stepanivna");
            studentDTO1.setEmail("romaniuk@gmail.com");
            StudentDTO studentDTO3 = new StudentDTO();
            studentDTO3.setName("Viktor"); studentDTO3.setSurname("Hanushchak"); studentDTO3.setPatronymic("Mykolaiovych");
            studentDTO3.setEmail("hanushchak@bigmir.net");

            when(studentMapper.studentImportDTOToStudent(studentImportDTO1)).thenReturn(student1);
            when(studentMapper.studentImportDTOToStudent(studentImportDTO3)).thenReturn(student3);
            when(studentMapper.studentDTOToStudent(studentDTO1)).thenReturn(student1);
            when(userService.automaticRegistration(studentDTO1.getEmail(), Role.ROLE_STUDENT)).thenReturn(userWithId1L);
            when(studentMapper.studentDTOToStudent(studentDTO3)).thenReturn(student3);
            when(userService.automaticRegistration(studentDTO3.getEmail(), Role.ROLE_STUDENT)).thenReturn(userWithId2L);
            when(studentService.save(studentDTO1)).thenReturn(student1);
            when(studentService.save(studentDTO3)).thenReturn(student3);
            when(groupService.getById(anyLong())).thenReturn(groupDTO);
            when(groupMapper.groupDTOToGroup(groupDTO)).thenReturn(group);
            when(studentMapper.studentToStudentImportDTO(student1registered)).thenReturn(studentImportDTO1);
            when(studentMapper.studentToStudentImportDTO(student3registered)).thenReturn(studentImportDTO3);
            // Act
            List<StudentImportDTO> actualStudents = studentService.saveFromFile(multipartFile, 4L).getNow(new ArrayList<>());
            // Assert
            assertNotNull(actualStudents);
            assertEquals(expectedStudents, actualStudents);
            verify(studentRepository).save(student1);
            verify(studentRepository).save(student3);
            verify(studentRepository).getExistingStudent(student1);
            verify(studentRepository).getExistingStudent(student3);
        }

        @Test
        void saveStudentFromFileSetsValidationErrorWhenEmailIsEmpty() {
            // Arrange — email is empty string → should hit VALIDATION_ERROR branch immediately
            StudentImportDTO studentWithEmptyEmail = buildImportDTO("John", "Doe", "Jr", "");
            // Act
            StudentImportDTO result = studentService.saveStudentFromFile(1L, studentWithEmptyEmail);
            // Assert
            assertEquals(ImportSaveStatus.VALIDATION_ERROR, result.getImportSaveStatus());
            verifyNoInteractions(userService, studentRepository, groupService);
        }

        @Test
        void saveStudentFromFileSetsValidationErrorWhenEmailIsNull() {
            // Arrange — null email → same VALIDATION_ERROR branch
            StudentImportDTO studentWithNullEmail = buildImportDTO("Jane", "Doe", "Jr", null);
            // Act
            StudentImportDTO result = studentService.saveStudentFromFile(1L, studentWithNullEmail);
            // Assert
            assertEquals(ImportSaveStatus.VALIDATION_ERROR, result.getImportSaveStatus());
            verifyNoInteractions(userService, studentRepository, groupService);
        }

        @Test
        void saveStudentFromFileSavesNewStudentAndSetsImportData() {
            Long groupId = 10L;

            StudentImportDTO dto = buildImportDTO(
                    "Hanna",
                    "Romaniuk",
                    "Stepanivna",
                    "romaniuk@gmail.com"
            );

            GroupDTO groupDTO = new GroupDTO();
            groupDTO.setId(groupId);
            groupDTO.setTitle("Test");

            Group group = buildGroup(groupId, "Test");

            Student mappedStudent = buildStudent(null, null, null);

            StudentImportDTO mappedBackDto = buildImportDTO(
                    "Hanna",
                    "Romaniuk",
                    "Stepanivna",
                    dto.getEmail()
            );

            User user = buildUser(1L, dto.getEmail());

            when(userService.findSocialUser(dto.getEmail()))
                    .thenReturn(Optional.empty());

            when(studentMapper.studentImportDTOToStudent(dto))
                    .thenReturn(mappedStudent);

            when(studentRepository.getExistingStudent(mappedStudent))
                    .thenReturn(Optional.empty());

            when(groupService.getById(groupId))
                    .thenReturn(groupDTO);

            when(groupMapper.groupDTOToGroup(groupDTO))
                    .thenReturn(group);

            when(userService.automaticRegistration(
                    dto.getEmail(),
                    Role.ROLE_STUDENT
            )).thenReturn(user);

            when(studentMapper.studentToStudentImportDTO(any(Student.class)))
                    .thenReturn(mappedBackDto);

            StudentImportDTO result =
                    studentService.saveStudentFromFile(groupId, dto);

            assertEquals(dto.getEmail(), result.getEmail());

            assertEquals(groupDTO, result.getGroupDTO());

            assertEquals(
                    ImportSaveStatus.SAVED,
                    result.getImportSaveStatus()
            );
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private User buildUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPassword("Pass1233!");
        user.setRole(Role.ROLE_STUDENT);
        return user;
    }

    private Group buildGroup(Long id, String title) {
        Group group = new Group();
        group.setId(id);
        group.setTitle(title);
        return group;
    }

    private Student buildStudent(Long id, User user, Group group) {
        Student student = new Student();
        student.setId(id);
        student.setName("Name");
        student.setSurname("Surname");
        student.setPatronymic("Patronymic");
        student.setUser(user);
        student.setGroup(group);
        return student;
    }

    private StudentImportDTO buildImportDTO(String name, String surname, String patronymic, String email) {
        StudentImportDTO dto = new StudentImportDTO();
        dto.setName(name);
        dto.setSurname(surname);
        dto.setPatronymic(patronymic);
        dto.setEmail(email);
        return dto;
    }
}
