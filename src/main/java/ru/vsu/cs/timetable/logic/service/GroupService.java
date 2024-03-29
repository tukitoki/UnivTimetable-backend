package ru.vsu.cs.timetable.logic.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import ru.vsu.cs.timetable.model.dto.group.GroupDto;
import ru.vsu.cs.timetable.model.dto.group.GroupPageDto;
import ru.vsu.cs.timetable.model.dto.group.GroupViewDto;
import ru.vsu.cs.timetable.model.dto.page.SortDirection;
import ru.vsu.cs.timetable.model.entity.Group;

@Validated
public interface GroupService {

    GroupViewDto getFacultyGroupsV2(Integer course, Integer groupNumber,
                                    SortDirection order, @NotNull Long facultyId);

    GroupPageDto getFacultyGroups(int currentPage, int pageSize, Integer course,
                                  Integer groupNumber, SortDirection order, @NotNull Long facultyId);

    GroupDto getGroupById(@NotNull Long id);

    Group findGroupById(@NotNull Long id);

    void createGroup(@NotNull @Valid GroupDto groupDto,
                     @NotNull Long facultyId);

    void deleteGroup(@NotNull Long id);

    void updateGroup(@NotNull @Valid GroupDto groupDto,
                     @NotNull Long id);
}
