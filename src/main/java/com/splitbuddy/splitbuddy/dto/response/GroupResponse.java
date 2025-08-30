package com.splitbuddy.splitbuddy.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class GroupResponse {
        private Long id;
        private String name;
        private Long createdBy;
        private String createdByName;
        private List<GroupMemberResponse> members;

        @Data
        public static class GroupMemberResponse {
                private Long id;
                private String name;
                private String email;
        }
}
