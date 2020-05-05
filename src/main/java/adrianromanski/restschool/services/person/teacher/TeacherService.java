package adrianromanski.restschool.services.person.teacher;

import adrianromanski.restschool.domain.base_entity.enums.Subjects;
import adrianromanski.restschool.model.base_entity.event.ExamDTO;
import adrianromanski.restschool.model.base_entity.person.StudentDTO;
import adrianromanski.restschool.model.base_entity.person.TeacherDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public interface TeacherService {

    // GET
    List<TeacherDTO> getAllTeachers();

    TeacherDTO getTeacherByFirstNameAndLastName(String firstName, String lastName);

    TeacherDTO getTeacherByID(Long id);

    Map<Subjects, List<TeacherDTO>> getTeachersBySpecialization();

    Map<Long, List<TeacherDTO>> getTeachersByYearsOfExperience();

    // POST
    ExamDTO addExamForClass(Long teacherID, ExamDTO examDTO);

    ExamDTO addCorrectionExamForStudent(Long teacherID, Long studentID, ExamDTO examDTO);

    StudentDTO addNewStudentToClass(Long teacherID, StudentDTO studentDTO);

    TeacherDTO createNewTeacher(TeacherDTO teacherDTO);

    // PUT
    ExamDTO moveExamToAnotherDay(Long teacherID, Integer examID, LocalDate date);

    TeacherDTO changeClassPresident(Long teacherID, Long studentID);

    TeacherDTO updateTeacher(Long id, TeacherDTO teacherDTO);

    // DELETE
    void deleteTeacherById(Long id);

    void removeStudentFromClass(Long teacherID, Long studentID);


}

