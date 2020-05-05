package adrianromanski.restschool.services.person.teacher;

import adrianromanski.restschool.domain.base_entity.enums.Subjects;
import adrianromanski.restschool.domain.base_entity.event.Exam;
import adrianromanski.restschool.domain.base_entity.person.Student;
import adrianromanski.restschool.domain.base_entity.person.Teacher;
import adrianromanski.restschool.exceptions.ResourceNotFoundException;
import adrianromanski.restschool.mapper.event.ExamMapper;
import adrianromanski.restschool.mapper.person.StudentMapper;
import adrianromanski.restschool.mapper.person.TeacherMapper;
import adrianromanski.restschool.model.base_entity.event.ExamDTO;
import adrianromanski.restschool.model.base_entity.person.StudentDTO;
import adrianromanski.restschool.model.base_entity.person.TeacherDTO;
import adrianromanski.restschool.repositories.event.ExamRepository;
import adrianromanski.restschool.repositories.person.StudentRepository;
import adrianromanski.restschool.repositories.person.TeacherRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


@Slf4j
@Service
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;
    private final TeacherMapper teacherMapper;
    private final ExamMapper examMapper;
    private final StudentMapper studentMapper;

    public TeacherServiceImpl(TeacherRepository teacherRepository, StudentRepository studentRepository, ExamRepository examRepository,
                              TeacherMapper teacherMapper, ExamMapper examMapper, StudentMapper studentMapper) {
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.examRepository = examRepository;
        this.teacherMapper = teacherMapper;
        this.examMapper = examMapper;
        this.studentMapper = studentMapper;
    }

    /**
     * @return all Teachers sorted by Specialization -> yearsOfExperience
     */
    @Override
    public List<TeacherDTO> getAllTeachers() {
        return teacherRepository.findAll()
                .stream()
                .map(teacherMapper::teacherToTeacherDTO)
                .sorted(Comparator
                        .comparing(
                                TeacherDTO::getSubject)
                        .thenComparing(
                                TeacherDTO::getYearsOfExperience
                        )
                )
                .collect(toList());
    }

    /**
     * @return Teacher with matching firstName and lastName
     * @throws ResourceNotFoundException if not found
     */
    @Override
    public TeacherDTO getTeacherByFirstNameAndLastName(String firstName, String lastName) {
        return teacherMapper.teacherToTeacherDTO(teacherRepository
                .getTeacherByFirstNameAndLastName(firstName, lastName)
                .orElseThrow(ResourceNotFoundException::new));
    }

    /**
     * @return Teacher with matching id
     * @throws ResourceNotFoundException if not found
     */
    @Override
    public TeacherDTO getTeacherByID(Long id) {
        return teacherMapper.teacherToTeacherDTO(teacherRepository
                .findById(id)
                .orElseThrow(ResourceNotFoundException::new));
    }

    /**
     * @return Map where the keys are Specializations and values List of Teachers
     */
    @Override
    public Map<Subjects, List<TeacherDTO>> getTeachersBySpecialization() {
        return teacherRepository.findAll()
                .stream()
                .map(teacherMapper::teacherToTeacherDTO)
                .collect(
                        Collectors.groupingBy(
                                TeacherDTO::getSubject
                        )
                );
    }

    /**
     * @return Map where the keys are years of experience and values List of Teachers
     */
    @Override
    public Map<Long, List<TeacherDTO>> getTeachersByYearsOfExperience() {
        return teacherRepository.findAll()
                .stream()
                .map(teacherMapper::teacherToTeacherDTO)
                .collect(
                        Collectors.groupingBy(
                                TeacherDTO::getYearsOfExperience
                        )
                );
    }

    /**
     * Adding Exam to every Student in the Class
     * @throws ResourceNotFoundException if teacher not found
     */
    @Override
    public ExamDTO addExamForClass(Long teacherID, ExamDTO examDTO) {
        if(teacherRepository.findById(teacherID).isPresent()) {
            Teacher teacher = teacherRepository.findById(teacherID).get();
            Exam exam = examMapper.examDTOToExam(examDTO);
            teacher.getExams().add(exam);   // Adding Exam to Teacher
            teacher.getStudentClass().getStudentList().forEach(s -> s.getExams().add(exam)); // Adding Exams to Students
            exam.setStudents(teacher.getStudentClass().getStudentList()); // Adding Students to Exam
            exam.setTeacher(teacher); // Adding Teacher to Exam
            teacherRepository.save(teacher);
            log.info("Teacher with id: " + teacherID + " saved to repository");
            examRepository.save(exam);
            log.info("Exam with id: " + exam.getId() + " saved to repository");
            return examMapper.examToExamDTO(exam);
        } else {
            log.debug("Teacher with id: " + teacherID + " not found");
            throw new ResourceNotFoundException("Teacher with id: " + teacherID + " not found");
        }
    }


    /**
     * Adding Correction Exam to Student with matching id
     * @throws ResourceNotFoundException if teacher not found
     * @throws ResourceNotFoundException if student not found
     */
    @Override
    public ExamDTO addCorrectionExamForStudent(Long teacherID, Long studentID, ExamDTO examDTO) {
        if (teacherRepository.findById(teacherID).isPresent()) {
            Teacher teacher = teacherRepository.findById(teacherID).get();
            log.info("Teacher with id: " + teacherID + " founded");
            Exam exam = examMapper.examDTOToExam(examDTO);
            if(studentRepository.findById(studentID).isPresent()) {
                Student student = studentRepository.findById(studentID).get();
                log.info("Student with id: " + studentID + " founded");
                teacher.getExams().add(exam); // Adding Exam to Teacher
                student.getExams().add(exam); // Adding Exam to Student
                exam.getStudents().add(student); // Adding Students to Exam
                exam.setTeacher(teacher); // Adding Teacher to Exam
                studentRepository.save(student);
                log.info("Student with id: " + studentID + " saved to repository");
                teacherRepository.save(teacher);
                log.info("Teacher with id: " + teacherID + " saved to repository");
                examRepository.save(exam);
                log.info("Exam with id: " + exam.getId() + " saved to repository");
                return examMapper.examToExamDTO(exam);
            } else {
                log.debug("Student with id: " + studentID + " not found");
                throw new ResourceNotFoundException("Student with id: " + studentID + " not found");
            }
        } else {
            log.debug("Teacher with id: " + teacherID + " not found");
            throw new ResourceNotFoundException("Teacher with id: " + teacherID + " not found");
        }
    }


    /**
     * Adding new Student to the Class
     * @throws ResourceNotFoundException if not found
     */
    @Override
    public StudentDTO addNewStudentToClass(Long teacherID, StudentDTO studentDTO) {
        if(teacherRepository.findById(teacherID).isPresent()) {
            Teacher teacher = teacherRepository.findById(teacherID).get();
            Student student = studentMapper.studentDTOToStudent(studentDTO);
            student.setStudentClass(teacher.getStudentClass()); // Adding StudentClass to Student
            teacher.getStudentClass().getStudentList().add(student); // Adding Student to StudentClass
            studentRepository.save(student);
            log.info("Student with id: " + student.getId() + " saved to repository");
            teacherRepository.save(teacher);
            log.info("Teacher with id: " + teacherID + " saved to repository");
            return studentMapper.studentToStudentDTO(student);
        }
        log.debug("Teacher with id: " + teacherID + " not found");
        throw new ResourceNotFoundException("Teacher with id: " + teacherID + " not found");
    }


    /**
     * Converts DTO Object and Save it to Database
     * @return TeacherDTO object
     */
    @Override
    public TeacherDTO createNewTeacher(TeacherDTO teacherDTO) {
        teacherRepository.save(teacherMapper.teacherDTOToTeacher(teacherDTO));
        log.info("Teacher with id: " + teacherDTO.getId() + " successfully saved");
        return teacherDTO;
    }


    /**
     * Moving exam from one day to another
     * @throws ResourceNotFoundException if not found
     */
    @Override
    public ExamDTO moveExamToAnotherDay(Long teacherID, Integer examID, LocalDate localDate) {
        if(teacherRepository.findById(teacherID).isPresent()) {
            Teacher teacher = teacherRepository.findById(teacherID).get();
            teacher.getExams().get(examID).setDate(localDate); //  Changing date
            teacherRepository.save(teacher);
            log.info("Teacher with id: " + teacherID + " saved to repository");
            return examMapper.examToExamDTO(teacher.getExams().get(examID));
        } else {
            log.debug("Teacher with id: " + teacherID + " not found");
            throw new ResourceNotFoundException("Teacher with id: " + teacherID + " not found");
        }
    }

    /**
     * Changing class president
     * @throws ResourceNotFoundException if student not found
     *  @throws ResourceNotFoundException if teacher not found
     */
    @Override
    public TeacherDTO changeClassPresident(Long teacherID, Long studentID) {
        if(teacherRepository.findById(teacherID).isPresent()) {
            Teacher teacher = teacherRepository.findById(teacherID).get();
            if(studentRepository.findById(studentID).isPresent()) {
                Student student = studentRepository.findById(studentID).get();
                teacher.getStudentClass().setPresident(student.getFirstName() + " " + student.getLastName());
                log.info("New president is Student with id: " + studentID);
                teacherRepository.save(teacher);
                return teacherMapper.teacherToTeacherDTO(teacher);
            } else {
                log.debug("Student with id: " + studentID + " not found");
                throw new ResourceNotFoundException("Student with id: " + studentID + " not found");
            }
        } else {
            log.debug("Teacher with id: " + teacherID + " not found");
            throw new ResourceNotFoundException("Teacher with id: " + teacherID + " not found");
        }
    }

    /**
     * Converts DTO Object, Update Teacher with Matching ID and save it to Database
     * @return TeacherDTO object if the student was successfully saved
     * @throws ResourceNotFoundException if not found
     */
    @Override
    public TeacherDTO updateTeacher(Long id, TeacherDTO teacherDTO) {
        if (teacherRepository.findById(id).isPresent()) {
            Teacher updatedTeacher = teacherMapper.teacherDTOToTeacher(teacherDTO);
            updatedTeacher.setId(id);
            teacherRepository.save(updatedTeacher);
            log.info("Student with id:" + id + " successfully updated");
            return teacherMapper.teacherToTeacherDTO(updatedTeacher);
        } else {
            throw new ResourceNotFoundException("Teacher with id: " + id + " not found");
        }
    }

    /**
     * Delete Teacher with matching id
     * @throws ResourceNotFoundException if not found
     */
    @Override
    public void deleteTeacherById(Long id) {
        if (teacherRepository.findById(id).isPresent()) {
            teacherRepository.deleteById(id);
            log.info("Teacher with id:" + id + " successfully deleted");
        } else {
            log.debug("Teacher with id: " + id + " not found");
            throw new ResourceNotFoundException("Teacher with id: " + id + " not found");
        }
    }


    /**
     * Removes a Student from the Class - it does not delete the Student from database
     * I will transform it later to some kind of punishment for bad behaviour
     */
    @Override
    public void removeStudentFromClass(Long teacherID, Long studentID) {
        if(teacherRepository.findById(teacherID).isPresent()) {
            Teacher teacher = teacherRepository.findById(teacherID).get();
            if(studentRepository.findById(studentID).isPresent()) {
                teacher.getStudentClass().getStudentList().remove(studentRepository.findById(studentID).get());
                log.info("Student with id: " + studentID + " removed from the class");
            } else {
                log.debug("Student with id: " + studentID + " not found");
                throw new ResourceNotFoundException("Teacher with id: " + teacherID + " not found");
            }
        } else {
            log.debug("Teacher with id: " + teacherID + " not found");
            throw new ResourceNotFoundException("Teacher with id: " + teacherID + " not found");
        }
    }
}

