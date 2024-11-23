package com.daniel.rediret.urlshortener;

import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UrlData {

    private String originalUrl;
    private Long expirationTime;
}
