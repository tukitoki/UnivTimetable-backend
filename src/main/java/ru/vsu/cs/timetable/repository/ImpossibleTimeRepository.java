package ru.vsu.cs.timetable.repository;

import org.springframework.data.repository.CrudRepository;
import ru.vsu.cs.timetable.model.entity.ImpossibleTime;

public interface ImpossibleTimeRepository extends CrudRepository<ImpossibleTime, Long> {
}
