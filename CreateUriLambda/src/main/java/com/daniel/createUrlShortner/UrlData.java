package com.daniel.createUrlShortner;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UrlData {

    private String originalUrl;
    private Long expirationTime;
}
