import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Question,
  QuestionRequest,
  Quiz,
  QuizDetail,
  QuizRequest,
  QuizTake,
  Submission,
  SubmissionDetail,
  SubmitQuizRequest,
} from '../models/quiz.model';

@Injectable({ providedIn: 'root' })
export class QuizService {
  private http = inject(HttpClient);
  private baseUrl = environment.quizApiUrl;

  // ---- Instructor: building quizzes ----

  createQuiz(courseId: number, request: QuizRequest): Observable<Quiz> {
    return this.http.post<Quiz>(`${this.baseUrl}/courses/${courseId}/quizzes`, request);
  }

  listQuizzesForCourse(courseId: number): Observable<Quiz[]> {
    return this.http.get<Quiz[]>(`${this.baseUrl}/courses/${courseId}/quizzes`);
  }

  getQuizDetail(quizId: number): Observable<QuizDetail> {
    return this.http.get<QuizDetail>(`${this.baseUrl}/quizzes/${quizId}`);
  }

  updateQuiz(quizId: number, request: QuizRequest): Observable<Quiz> {
    return this.http.put<Quiz>(`${this.baseUrl}/quizzes/${quizId}`, request);
  }

  deleteQuiz(quizId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/quizzes/${quizId}`);
  }

  addQuestion(quizId: number, request: QuestionRequest): Observable<Question> {
    return this.http.post<Question>(`${this.baseUrl}/quizzes/${quizId}/questions`, request);
  }

  updateQuestion(questionId: number, request: QuestionRequest): Observable<Question> {
    return this.http.put<Question>(`${this.baseUrl}/questions/${questionId}`, request);
  }

  deleteQuestion(questionId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/questions/${questionId}`);
  }

  listSubmissionsForQuiz(quizId: number): Observable<Submission[]> {
    return this.http.get<Submission[]>(`${this.baseUrl}/quizzes/${quizId}/submissions`);
  }

  // ---- Student: taking quizzes ----

  getQuizToTake(quizId: number): Observable<QuizTake> {
    return this.http.get<QuizTake>(`${this.baseUrl}/quizzes/${quizId}/take`);
  }

  submit(quizId: number, request: SubmitQuizRequest): Observable<SubmissionDetail> {
    return this.http.post<SubmissionDetail>(`${this.baseUrl}/quizzes/${quizId}/submit`, request);
  }

  listMySubmissions(quizId: number): Observable<Submission[]> {
    return this.http.get<Submission[]>(`${this.baseUrl}/quizzes/${quizId}/submissions/mine`);
  }
}
