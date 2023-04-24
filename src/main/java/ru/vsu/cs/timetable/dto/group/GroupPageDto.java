package ru.vsu.cs.timetable.dto.group;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.vsu.cs.timetable.dto.page.PageModel;

import java.util.List;

@Setter
@Getter
@SuperBuilder
public class GroupPageDto {

    private PageModel<GroupDto> groupsPage;
    private List<Integer> courses;
}
