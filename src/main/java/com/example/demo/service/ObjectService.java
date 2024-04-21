package com.example.demo.service;

import com.example.demo.entity.Confirm;
import com.example.demo.repository.ConfirmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ObjectService {
    @Autowired
    private ConfirmRepository repository;

    // 获取单个对象
    public Confirm getObjectById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    // 列出所有对象
    public List<Confirm> listAllObjects() {
        return repository.findAll();
    }

    // 创建对象
    public Confirm createObject(Confirm confirm) {
        return repository.save(confirm);
    }

    // 更新对象
    public Optional<Optional<?>> updateObject(Integer id, Map<String, String> updateParams, String username) {
        return repository.findById(id).map(confirm -> {
            if (confirm.getCreateUser().equals(username)) {
                if (updateParams.containsKey("公司")) {
                    confirm.setCompany(updateParams.get("公司"));
                }
                if (updateParams.containsKey("岗位")) {
                    confirm.setPosition(updateParams.get("岗位"));
                }
                if (updateParams.containsKey("城市")) {
                    confirm.setCity(updateParams.get("城市"));
                }
                if (updateParams.containsKey("薪资")) {
                    confirm.setSalary(Integer.parseInt(updateParams.get("薪资")));
                }
                if (updateParams.containsKey("学历")) {
                    confirm.setEducation(updateParams.get("学历"));
                }
                if (updateParams.containsKey("行业")) {
                    confirm.setIndustry(updateParams.get("行业"));
                }
                if (updateParams.containsKey("类型")) {
                    confirm.setType(updateParams.get("类型"));
                }
                if (updateParams.containsKey("备注")) {
                    confirm.setRemarks(updateParams.get("备注"));
                }
                repository.save(confirm);
                return Optional.of(confirm);
            }
            return Optional.empty();  // 如果不是创建者，返回空
        });
    }


    // 删除对象
    public boolean deleteObject(Integer id, String username) {
        return repository.findById(id).map(confirm -> {
            if (confirm.getCreateUser().equals(username)) {
                repository.deleteById(id);
                return true;
            }
            return false;
        }).orElse(false);
    }

    // 替换对象
    public Optional<Confirm> replaceObject(Integer id, Confirm newConfirm, String username) {
        return repository.findById(id).map(confirm -> {
            if (confirm.getCreateUser().equals(username)) {
                newConfirm.setId(id);
                return repository.save(newConfirm);
            }
            return null;  // 如果不是创建者，返回null
        });
    }

    // 分页列出对象
    public Page<Confirm> listObjectsPaginated(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    // 根据条件搜索对象
    public Page<Confirm> searchConfirms(Map<String, String> searchParams, Pageable pageable) {
        Specification<Confirm> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            searchParams.forEach((key, value) -> {
                if (value != null && !value.isEmpty()) {
                    switch (key) {
                        case "公司":
                            predicates.add(criteriaBuilder.like(root.get("company"), "%" + value + "%"));
                            break;
                        case "岗位":
                            predicates.add(criteriaBuilder.like(root.get("position"), "%" + value + "%"));
                            break;
                        case "城市":
                            predicates.add(criteriaBuilder.like(root.get("city"), "%" + value + "%"));
                            break;
                        case "薪资":
                            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("salary"), Integer.parseInt(value)));
                            break;
                        case "学历":
                            predicates.add(criteriaBuilder.like(root.get("education"), "%" + value + "%"));
                            break;
                        case "行业":
                            predicates.add(criteriaBuilder.like(root.get("industry"), "%" + value + "%"));
                            break;
                        case "类型":
                            predicates.add(criteriaBuilder.equal(root.get("type"), value));
                            break;
                        case "备注":
                            predicates.add(criteriaBuilder.like(root.get("remarks"), "%" + value + "%"));
                            break;
                    }
                }
            });
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return repository.findAll(spec, pageable);
    }

    // 批量创建对象
    public List<Confirm> createObjects(List<Confirm> confirms) {
        return repository.saveAll(confirms);
    }



}
