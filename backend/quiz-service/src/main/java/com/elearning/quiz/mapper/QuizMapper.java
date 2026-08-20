package com.elearning.quiz.mapper;

import com.elearning.quiz.dto.response.*;
import com.elearning.quiz.entity.Choice;
import com.elearning.quiz.entity.Question;
import com.elearning.quiz.entity.Quiz;
import com.elearning.quiz.entity.Submission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuizMapper {

    @Mapping(target = "questionCount", expression = "java(quiz.getQuestions().size())")
    QuizResponse toResponse(Quiz quiz);

    ChoiceResponse toChoiceResponse(Choice choice);

    QuestionResponse toQuestionResponse(Question question);

    default QuizDetailResponse toDetailResponse(Quiz quiz) {
        List<QuestionResponse> questions = quiz.getQuestions().stream()
                .map(this::toQuestionResponse)
                .toList();
        return QuizDetailResponse.builder()
                .quiz(toResponse(quiz))
                .questions(questions)
                .build();
    }

    ChoiceTakeResponse toChoiceTakeResponse(Choice choice);

    QuestionTakeResponse toQuestionTakeResponse(Question question);

    default QuizTakeResponse toTakeResponse(Quiz quiz) {
        List<QuestionTakeResponse> questions = quiz.getQuestions().stream()
                .map(this::toQuestionTakeResponse)
                .toList();
        return QuizTakeResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .passingScorePercent(quiz.getPassingScorePercent())
                .questions(questions)
                .build();
    }

    @Mapping(target = "quizId", expression = "java(submission.getQuiz().getId())")
    SubmissionResponse toSubmissionResponse(Submission submission);
}
