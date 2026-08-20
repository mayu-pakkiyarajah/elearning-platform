import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Submission, SubmissionDetail, SubmitQuizRequest } from '../../../core/models/quiz.model';
import { QuizTake } from '../../../core/models/quiz.model';
import { QuizService } from '../../../core/services/quiz.service';

@Component({
  selector: 'app-quiz-take',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './quiz-take.component.html',
})
export class QuizTakeComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private quizService = inject(QuizService);

  private quizId!: number;
  readonly courseSlug = signal<string | null>(null);

  readonly quiz = signal<QuizTake | null>(null);
  readonly isLoading = signal(true);
  readonly notEnrolled = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly answers = signal<Record<number, number>>({});
  readonly result = signal<SubmissionDetail | null>(null);
  readonly isSubmitting = signal(false);
  readonly pastAttempts = signal<Submission[]>([]);

  ngOnInit(): void {
    this.quizId = Number(this.route.snapshot.paramMap.get('quizId'));
    this.courseSlug.set(this.route.snapshot.paramMap.get('slug'));

    this.quizService.getQuizToTake(this.quizId).subscribe({
      next: (quiz) => {
        this.quiz.set(quiz);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        if (err.status === 403) {
          this.notEnrolled.set(true);
        } else {
          this.errorMessage.set('Could not load this quiz.');
        }
      },
    });

    this.quizService.listMySubmissions(this.quizId).subscribe({
      next: (attempts) => this.pastAttempts.set(attempts),
      error: () => {},
    });
  }

  selectAnswer(questionId: number, choiceId: number): void {
    this.answers.update((current) => ({ ...current, [questionId]: choiceId }));
  }

  isSelected(questionId: number, choiceId: number): boolean {
    return this.answers()[questionId] === choiceId;
  }

  get answeredCount(): number {
    return Object.keys(this.answers()).length;
  }

  submit(): void {
    const quiz = this.quiz();
    if (!quiz) return;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const request: SubmitQuizRequest = {
      answers: quiz.questions.map((q) => ({
        questionId: q.id,
        choiceId: this.answers()[q.id] ?? null,
      })),
    };

    this.quizService.submit(this.quizId, request).subscribe({
      next: (detail) => {
        this.isSubmitting.set(false);
        this.result.set(detail);
        this.pastAttempts.update((attempts) => [detail.submission, ...attempts]);
      },
      error: () => {
        this.isSubmitting.set(false);
        this.errorMessage.set('Could not submit your answers — please try again.');
      },
    });
  }

  retake(): void {
    this.result.set(null);
    this.answers.set({});
  }

  correctChoiceIdFor(questionId: number): number | null {
    return this.result()?.answers.find((a) => a.questionId === questionId)?.correctChoiceId ?? null;
  }

  wasCorrect(questionId: number): boolean {
    return this.result()?.answers.find((a) => a.questionId === questionId)?.correct ?? false;
  }
}
