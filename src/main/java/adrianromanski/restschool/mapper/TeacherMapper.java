package adrianromanski.restschool.mapper;

import adrianromanski.restschool.domain.base_entity.person.Teacher;
import adrianromanski.restschool.model.base_entity.person.TeacherDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TeacherMapper {

    TeacherMapper INSTANCE = Mappers.getMapper(TeacherMapper.class);

    @Mappings({
            @Mapping(source = "studentClass", target = "studentClassDTO"),
            @Mapping(source = "exams", target = "examsDTO")
    })
    TeacherDTO teacherToTeacherDTO(Teacher teacher);

    @Mappings({
            @Mapping(source = "studentClassDTO", target = "studentClass"),
            @Mapping(source = "examsDTO", target = "exams")
    })
    Teacher teacherDTOToTeacher(TeacherDTO teacherDTO);
}
