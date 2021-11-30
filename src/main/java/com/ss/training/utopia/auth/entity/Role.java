package com.ss.training.utopia.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name="user_role")
public class Role {
    @Id
    @Column(name="id")
    private Integer id;

    @Column(name="name")
    private String roleName;
}
