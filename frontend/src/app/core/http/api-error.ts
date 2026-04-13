import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export interface ApiErrorPayload {
  status: number;
  message: string;
  validationErrors?: Record<string, string>;
}

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
    public readonly validationErrors?: Record<string, string>,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export const apiErrorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        return throwError(() => toApiError(error));
      }
      return throwError(() => error);
    }),
  );

function toApiError(response: HttpErrorResponse): ApiError {
  const body = response.error as Partial<ApiErrorPayload> | null;
  const message =
    body?.message ??
    (response.status === 0 ? 'No se pudo contactar al servidor.' : 'Ocurrió un error inesperado.');
  return new ApiError(response.status, message, body?.validationErrors);
}
