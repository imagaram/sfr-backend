package com.sfr.tokyo.sfr_backend.mapper.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningQuizDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningQuiz;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface LearningQuizMapper {

    /**
     * Entity → DTO変換（レスポンス用）
     */
    @Mappings({
            @Mapping(source = "questions", target = "questions", qualifiedByName = "questionsToDto"),
            @Mapping(target = "questionCount", expression = "java(entity.getQuestionCount())")
    })
    LearningQuizDto toDto(LearningQuiz entity);

    /**
     * Entity List → DTO List変換
     */
    List<LearningQuizDto> toDtoList(List<LearningQuiz> entities);

    /**
     * DTO → Entity変換（作成用）
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(source = "questions", target = "questions", qualifiedByName = "questionsToEntity"),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true)
    })
    LearningQuiz toEntity(LearningQuizDto dto);

    /**
     * Entity の QuizQuestion → DTO の QuizQuestionDto 変換
     */
    @Named("questionsToDto")
    default List<LearningQuizDto.QuizQuestionDto> questionsToDto(List<LearningQuiz.QuizQuestion> questions) {
        if (questions == null) {
            return null;
        }
        return questions.stream()
                .map(q -> new LearningQuizDto.QuizQuestionDto(q.getQuestion(), q.getOptions(), q.getAnswer()))
                .collect(Collectors.toList());
    }

    /**
     * DTO の QuizQuestionDto → Entity の QuizQuestion 変換
     */
    @Named("questionsToEntity")
    default List<LearningQuiz.QuizQuestion> questionsToEntity(List<LearningQuizDto.QuizQuestionDto> questionDtos) {
        if (questionDtos == null) {
            return null;
        }
        return questionDtos.stream()
                .map(dto -> new LearningQuiz.QuizQuestion(dto.getQuestion(), dto.getOptions(), dto.getAnswer()))
                .collect(Collectors.toList());
    }

    /**
     * 単一の QuizQuestion → QuizQuestionDto 変換
     */
    default LearningQuizDto.QuizQuestionDto questionToDto(LearningQuiz.QuizQuestion question) {
        if (question == null) {
            return null;
        }
        return new LearningQuizDto.QuizQuestionDto(question.getQuestion(), question.getOptions(), question.getAnswer());
    }

    /**
     * 単一の QuizQuestionDto → QuizQuestion 変換
     */
    default LearningQuiz.QuizQuestion questionToEntity(LearningQuizDto.QuizQuestionDto dto) {
        if (dto == null) {
            return null;
        }
        return new LearningQuiz.QuizQuestion(dto.getQuestion(), dto.getOptions(), dto.getAnswer());
    }
}
