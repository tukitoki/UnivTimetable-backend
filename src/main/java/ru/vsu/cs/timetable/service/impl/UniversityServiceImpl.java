package ru.vsu.cs.timetable.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.timetable.dto.page.PageModel;
import ru.vsu.cs.timetable.dto.page.SortDirection;
import ru.vsu.cs.timetable.dto.university.CreateUnivRequest;
import ru.vsu.cs.timetable.dto.university.UniversityDto;
import ru.vsu.cs.timetable.dto.university.UniversityPageDto;
import ru.vsu.cs.timetable.exception.UniversityException;
import ru.vsu.cs.timetable.mapper.UniversityMapper;
import ru.vsu.cs.timetable.model.University;
import ru.vsu.cs.timetable.repository.UniversityRepository;
import ru.vsu.cs.timetable.repository.UserRepository;
import ru.vsu.cs.timetable.service.UniversityService;

import java.util.ArrayList;
import java.util.List;

import static ru.vsu.cs.timetable.dto.page.SortDirection.ASC;

@RequiredArgsConstructor
@Service
public class UniversityServiceImpl implements UniversityService {

    private final UniversityRepository universityRepository;
    private final UserRepository userRepository;
    private final UniversityMapper universityMapper;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public UniversityPageDto getAllUniversities(int pageNumber, int pageSize,
                                                String universityName, SortDirection order) {
        Page<University> page = filerPage(pageNumber, pageSize, universityName, order);

        List<UniversityDto> universityDtos = page.getContent()
                .stream()
                .map(universityMapper::toDto)
                .toList();

        var pageModel = PageModel.of(universityDtos, pageNumber, page.getTotalElements(),
                pageSize, page.getTotalPages());

        return UniversityPageDto.builder()
                .universitiesPage(pageModel)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UniversityDto getUniversityById(Long id) {
        University university = findUnivById(id);

        return universityMapper.toDto(university);
    }

    @Override
    public University findUnivById(Long id) {
        return universityRepository.findById(id)
                .orElseThrow(UniversityException.CODE.ID_NOT_FOUND::get);
    }

    @Override
    public University findUnivByName(String name) {
        return universityRepository.findByNameIgnoreCase(name)
                .orElseThrow(UniversityException.CODE.UNIVERSITY_ALREADY_PRESENT::get);
    }

    @Override
    public List<University> findAllUniversities() {
        return universityRepository.findAll();
    }

    @Override
    public void createUniversity(CreateUnivRequest createUnivRequest) {
        if (universityRepository.findByName(createUnivRequest.getUniversityName()).isPresent()) {
            throw UniversityException.CODE.UNIVERSITY_ALREADY_PRESENT.get();
        }

        University university = University.builder()
                .name(createUnivRequest.getUniversityName())
                .city(createUnivRequest.getCity())
                .build();

        universityRepository.save(university);
    }

    @Override
    public void updateUniversity(UniversityDto universityDto, Long id) {
        var oldUniv = findUnivById(id);
        var newUniv = universityMapper.toEntity(universityDto);

        BeanUtils.copyProperties(newUniv, oldUniv, "id", "users", "faculties", "audiences");
        universityRepository.save(oldUniv);
    }

    @Override
    @Transactional
    public void deleteUniversity(Long id) {
        University university = findUnivById(id);
        university.getUsers().forEach(user -> {
            user.setUniversity(null);
            user.setFaculty(null);
            user.setGroup(null);
            userRepository.save(user);
        });

        universityRepository.delete(university);
    }

    private Page<University> filerPage(int pageNumber, int pageSize,
                                       String universityName, SortDirection order) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<University> query = cb.createQuery(University.class);

        Root<University> root = query.from(University.class);
        query.select(root).distinct(true);

        List<Predicate> predicates = new ArrayList<>();
        if (universityName != null) {
            predicates.add(cb.like(cb.lower(root.get("name").as(String.class)), universityName));
        }
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        Path<Object> name = root.get("name");
        Order alphabetOrder = order.equals(ASC) ? cb.asc(name) : cb.desc(name);
        List<Order> orderList = List.of(alphabetOrder, cb.asc(root.get("id")));

        query.orderBy(orderList);

        TypedQuery<University> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<University> universities = typedQuery.getResultList();

        long count = countFilteredUsers(universityName);

        return new PageImpl<>(universities, pageable, count);
    }


    private long countFilteredUsers(String universityName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<University> root = query.from(University.class);
        query.select(cb.countDistinct(root));

        List<Predicate> predicates = new ArrayList<>();
        if (universityName != null) {
            predicates.add(cb.like(cb.lower(root.get("name").as(String.class)), universityName));
        }
        query.where(cb.and(predicates.toArray(new Predicate[0])));
        TypedQuery<Long> typedQuery = entityManager.createQuery(query);

        return typedQuery.getSingleResult();
    }
}
