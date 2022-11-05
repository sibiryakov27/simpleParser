package com.simpleparser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.net.URI;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class Post {
    private URI url;
    private String header;
    private Date date;
    private String authorNickname;
    private List<String> habs;
    private Integer rating;
}
