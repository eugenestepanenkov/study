package com.stepsoft.study.data.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.SQLDelete;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Set;

import static com.stepsoft.study.data.utils.EntityConstants.SINNER;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;

/**
 * @author Eugene Stepanenkov
 */
@Entity
@SQLDelete(sql = "update sinner set isProcessed=true where id")
public class Sinner {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = SINNER, cascade = {ALL, PERSIST, MERGE})
    private Set<Karma> karmas;

    private String userName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Karma> getKarmas() {
        return karmas;
    }

    public void setKarmas(Set<Karma> karma) {
        this.karmas = karma;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Sinner sinner = (Sinner) o;

        return new EqualsBuilder()
                .append(id, sinner.id)
                .append(karmas, sinner.karmas)
                .append(userName, sinner.userName)
                .isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(karmas)
                .append(userName)
                .toHashCode();
    }
}
