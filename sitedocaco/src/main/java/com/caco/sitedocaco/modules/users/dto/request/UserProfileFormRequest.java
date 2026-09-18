package com.caco.sitedocaco.modules.users.dto.request;

import com.caco.sitedocaco.shared.entity.CourseType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserProfileFormRequest(

        @NotNull(message = "O campo 'course' é obrigatório.")
        CourseType course,

        @Size(max = 50, message = "O nome do curso deve ter no máximo 50 caracteres.")
        String otherCourseName,

        @NotNull(message = "O campo 'entryYear' é obrigatório.")
        Integer entryYear
) {
}

