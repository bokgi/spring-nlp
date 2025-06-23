package com.adacho.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gpt_cache")
@Getter
@Setter
@NoArgsConstructor
public class GptCache {
	
    @Id
    @Column(name = "hash", length = 64)
    private String hash;

    @Lob
    @Column(name = "restaurant_list")
    private String restaurantListJson;

    @Lob
    @Column(name = "gpt_response_list")
    private String gptResponseListJson;
}
