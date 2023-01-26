package com.training.eshop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "attachment")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Attachment implements Serializable {

    private static final long serialVersionUID = 3906771677381811334L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Lob
    @Column(name = "file", columnDefinition = "blob")
    @ToString.Exclude
    @JsonIgnore
    private byte[] file;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    @ToString.Exclude
    @JsonIgnore
    private Order order;

    public Attachment(String name, byte[] file) {
        this.name = name;
        this.file = file;
    }
}
