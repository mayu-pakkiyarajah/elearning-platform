import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiErrorResponse } from '../../../core/models/auth.model';
import { QuestionRequest, Quiz, QuizDetail, QuizRequest } from '../../../core/models/quiz.model';
import { QuizService } from '../../../core/services/quiz.service';
import { AlertComponent } from '../../../shared/components/alert/alert.component';

@Component({
  selector: 'app-quiz-manager',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AlertComponent],
  templateUrl: './quiz-manager.component.html',
})
export class QuizManagerComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private quizService = inject(QuizService);

  private courseId!: number;

  readonly quizzes = signal<Quiz[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  readonly expandedQuizId = signal<number | null>(null);
  readonly expandedDetail = signal<QuizDetail | null>(null);
  readonly isLoadingDetail = signal(false);

  readonly addingQuiz = signal(false);
  readonly editingQuizId = signal<number | null>(null);
  readonly addingQuestionForQuiz = signal<number | null>(null);

  quizForm = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    description: [''],
    passingScorePercent: [70, [Validators.required, Validators.min(0), Validators.max(100)]],
  });

  questionForm = this.fb.group({
    text: ['', [Validators.required]],
    points: [1, [Validators.required, Validators.min(1)]],
    choices: this.fb.array([
      this.fb.control('', Validators.required),
      this.fb.control('', Validators.required),
    ]),
    correctIndex: [0, [Validators.required]],
  });

  get choiceControls(): FormArray {
    return this.questionForm.get('choices') as FormArray;
  }

  ngOnInit(): void {
    this.courseId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  private load(): void {
    this.isLoading.set(true);
    this.quizService.listQuizzesForCourse(this.courseId).subscribe({
      next: (quizzes) => {
        this.quizzes.set(quizzes);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  // ---- Quiz CRUD ----

  startAddQuiz(): void {
    this.quizForm.reset({ title: '', description: '', passingScorePercent: 70 });
    this.addingQuiz.set(true);
  }

  submitNewQuiz(): void {
    if (this.quizForm.invalid) {
      this.quizForm.markAllAsTouched();
      return;
    }
    this.quizService.createQuiz(this.courseId, this.quizForm.getRawValue() as QuizRequest).subscribe({
      next: () => {
        this.addingQuiz.set(false);
        this.load();
      },
      error: (err: HttpErrorResponse) => this.showError(err),
    });
  }

  startEditQuiz(quiz: Quiz): void {
    this.quizForm.reset({
      title: quiz.title,
      description: quiz.description ?? '',
      passingScorePercent: quiz.passingScorePercent,
    });
    this.editingQuizId.set(quiz.id);
  }

  submitEditQuiz(quiz: Quiz): void {
    if (this.quizForm.invalid) {
      this.quizForm.markAllAsTouched();
      return;
    }
    this.quizService.updateQuiz(quiz.id, this.quizForm.getRawValue() as QuizRequest).subscribe({
      next: () => {
        this.editingQuizId.set(null);
        this.load();
      },
      error: (err: HttpErrorResponse) => this.showError(err),
    });
  }

  deleteQuiz(quiz: Quiz): void {
    if (!confirm(`Delete quiz "${quiz.title}" and all its questions?`)) return;
    this.quizService.deleteQuiz(quiz.id).subscribe({
      next: () => this.load(),
      error: (err: HttpErrorResponse) => this.showError(err),
    });
  }

  // ---- Expand / questions ----

  toggleExpand(quiz: Quiz): void {
    if (this.expandedQuizId() === quiz.id) {
      this.expandedQuizId.set(null);
      this.expandedDetail.set(null);
      return;
    }
    this.expandedQuizId.set(quiz.id);
    this.isLoadingDetail.set(true);
    this.quizService.getQuizDetail(quiz.id).subscribe({
      next: (detail) => {
        this.expandedDetail.set(detail);
        this.isLoadingDetail.set(false);
      },
      error: () => this.isLoadingDetail.set(false),
    });
  }

  private refreshExpandedDetail(): void {
    const quizId = this.expandedQuizId();
    if (!quizId) return;
    this.quizService.getQuizDetail(quizId).subscribe((detail) => this.expandedDetail.set(detail));
    this.load(); // keep question counts in the outer list in sync
  }

  startAddQuestion(quizId: number): void {
    this.questionForm.reset({ text: '', points: 1, correctIndex: 0 });
    while (this.choiceControls.length > 2) this.choiceControls.removeAt(0);
    this.choiceControls.at(0).setValue('');
    this.choiceControls.at(1).setValue('');
    this.addingQuestionForQuiz.set(quizId);
  }

  addChoiceField(): void {
    this.choiceControls.push(this.fb.control('', Validators.required));
  }

  removeChoiceField(index: number): void {
    if (this.choiceControls.length <= 2) return;
    this.choiceControls.removeAt(index);
    const correctIndex = this.questionForm.value.correctIndex ?? 0;
    if (correctIndex === index) {
      this.questionForm.patchValue({ correctIndex: 0 });
    } else if (correctIndex > index) {
      this.questionForm.patchValue({ correctIndex: correctIndex - 1 });
    }
  }

  submitNewQuestion(quizId: number): void {
    if (this.questionForm.invalid) {
      this.questionForm.markAllAsTouched();
      return;
    }
    const raw = this.questionForm.getRawValue();
    const correctIndex = raw.correctIndex ?? 0;

    const request: QuestionRequest = {
      text: raw.text!,
      points: raw.points!,
      position: (this.expandedDetail()?.questions.length ?? 0) + 1,
      choices: (raw.choices as string[]).map((text, i) => ({
        text,
        position: i + 1,
        correct: i === correctIndex,
      })),
    };

    this.quizService.addQuestion(quizId, request).subscribe({
      next: () => {
        this.addingQuestionForQuiz.set(null);
        this.refreshExpandedDetail();
      },
      error: (err: HttpErrorResponse) => this.showError(err),
    });
  }

  deleteQuestion(questionId: number): void {
    if (!confirm('Delete this question?')) return;
    this.quizService.deleteQuestion(questionId).subscribe({
      next: () => this.refreshExpandedDetail(),
      error: (err: HttpErrorResponse) => this.showError(err),
    });
  }

  cancelAll(): void {
    this.addingQuiz.set(false);
    this.editingQuizId.set(null);
    this.addingQuestionForQuiz.set(null);
  }

  private showError(err: HttpErrorResponse): void {
    const apiError = err.error as ApiErrorResponse | undefined;
    this.errorMessage.set(apiError?.message ?? 'Something went wrong. Please try again.');
  }
}
