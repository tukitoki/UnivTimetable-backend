package ru.vsu.cs.timetable.logic.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.timetable.model.dto.page.PageModel;
import ru.vsu.cs.timetable.model.dto.page.SortDirection;
import ru.vsu.cs.timetable.model.dto.university.UniversityDto;
import ru.vsu.cs.timetable.model.dto.university.UniversityPageDto;
import ru.vsu.cs.timetable.model.entity.University;
import ru.vsu.cs.timetable.exception.UniversityException;
import ru.vsu.cs.timetable.logic.service.UniversityService;
import ru.vsu.cs.timetable.model.mapper.UniversityMapper;
import ru.vsu.cs.timetable.repository.UniversityRepository;
import ru.vsu.cs.timetable.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static ru.vsu.cs.timetable.model.dto.page.SortDirection.ASC;

@RequiredArgsConstructor
@Slf4j
@Transactional
@Service
public class UniversityServiceImpl implements UniversityService {

    private final UniversityMapper universityMapper;
    private final UniversityRepository universityRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<UniversityDto> getAllUniversitiesV2(String universityName, SortDirection order) {
        Page<University> page = filerPage(1, 10, universityName, order, false);
        return page.getContent()
                .stream()
                .map(universityMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UniversityPageDto getAllUniversities(int currentPage, int pageSize,
                                                String universityName, SortDirection order) {
        Page<University> page = filerPage(currentPage, pageSize, universityName, order, true);

        List<UniversityDto> universityDtos = page.getContent()
                .stream()
                .map(universityMapper::toDto)
                .toList();

        var pageModel = PageModel.of(universityDtos, currentPage, page.getTotalElements(),
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
    @Transactional(readOnly = true)
    public University findUnivById(Long id) {
        return universityRepository.findById(id)
                .orElseThrow(UniversityException.CODE.ID_NOT_FOUND::get);
    }

    @Override
    @Transactional(readOnly = true)
    public University findUnivByName(String name) {
        return universityRepository.findByNameIgnoreCase(name)
                .orElseThrow(UniversityException.CODE.UNIVERSITY_ALREADY_PRESENT::get);
    }

    @Override
    @Transactional(readOnly = true)
    public List<University> findAllUniversities() {
        return universityRepository.findAll();
    }

    @Override
    public void createUniversity(UniversityDto universityDto) {
        if (universityRepository.findByName(universityDto.getUniversityName()).isPresent()) {
            throw UniversityException.CODE.UNIVERSITY_ALREADY_PRESENT.get();
        }

        University university = University.builder()
                .name(universityDto.getUniversityName())
                .city(universityDto.getCity())
                .build();

        university = universityRepository.save(university);

        log.info("university: {}, was successful saved", university);
    }

    @Override
    public void updateUniversity(UniversityDto universityDto, Long id) {
        University oldUniv = findUnivById(id);

        if (!oldUniv.getName().equals(universityDto.getUniversityName())
                && universityRepository.findByName(universityDto.getUniversityName()).isPresent()) {
            throw UniversityException.CODE.UNIVERSITY_ALREADY_PRESENT.get();
        }

        University newUniv = universityMapper.toEntity(universityDto);

        BeanUtils.copyProperties(newUniv, oldUniv, "id", "users", "faculties", "audiences");
        oldUniv = universityRepository.save(oldUniv);

        log.info("university: {}, was successful updated", oldUniv);

    }

    @Override
    public void deleteUniversity(Long id) {
        University university = findUnivById(id);
        university.getUsers().forEach(user -> {
            user.setUniversity(null);
            user.setFaculty(null);
            user.setGroup(null);
            userRepository.save(user);
        });

        universityRepository.delete(university);

        log.info("university: {}, was successful deleted", university);
    }

    private Page<University> filerPage(int currentPage, int pageSize, String universityName,
                                       SortDirection order, boolean isPageable) {
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<University> query = cb.createQuery(University.class);

        Root<University> root = query.from(University.class);
        query.select(root).distinct(true);

        List<Predicate> predicates = new ArrayList<>();
        if (universityName != null) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + universityName.toLowerCase() + "%"));
        }
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        Path<Object> name = root.get("name");
        Order alphabetOrder = order.equals(ASC)
                ? cb.asc(name)
                : cb.desc(name);

        List<Order> orderList = List.of(alphabetOrder, cb.asc(root.get("id")));
        query.orderBy(orderList);

        TypedQuery<University> typedQuery = entityManager.createQuery(query);
        if (isPageable) {
            typedQuery.setFirstResult((int) pageable.getOffset());
            typedQuery.setMaxResults(pageable.getPageSize());
        }

        List<University> universities = typedQuery.getResultList();

        long count = countFilteredUniversities(universityName);

        return new PageImpl<>(
                universities,
                isPageable
                        ? pageable
                        : PageRequest.of
                        (
                                currentPage - 1,
                                count > 1
                                        ? (int) count
                                        : 1
                        ),
                count
        );
    }


    private long countFilteredUniversities(String universityName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<University> root = query.from(University.class);
        query.select(cb.countDistinct(root));

        List<Predicate> predicates = new ArrayList<>();
        if (universityName != null) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + universityName.toLowerCase() + "%"));
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        TypedQuery<Long> typedQuery = entityManager.createQuery(query);

        return typedQuery.getSingleResult();
    }
}
