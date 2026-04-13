import { AbstractControl, ValidationErrors } from '@angular/forms';

type ErrorMessageBuilder = (error: unknown) => string;

const MESSAGES: Record<string, ErrorMessageBuilder> = {
  required: () => 'Este campo es obligatorio.',
  email: () => 'Ingresa un correo válido.',
  minlength: (e) => {
    const { requiredLength } = e as { requiredLength: number };
    return `Debe tener al menos ${requiredLength} caracteres.`;
  },
  maxlength: (e) => {
    const { requiredLength } = e as { requiredLength: number };
    return `No debe superar ${requiredLength} caracteres.`;
  },
  min: (e) => {
    const { min } = e as { min: number };
    return `El valor mínimo es ${min}.`;
  },
  max: (e) => {
    const { max } = e as { max: number };
    return `El valor máximo es ${max}.`;
  },
  pattern: () => 'El formato no es válido.',
  server: (e) => String(e),
};

export function getErrorMessage(control: AbstractControl | null): string | null {
  if (!control || !control.errors || (!control.touched && !control.dirty)) {
    return null;
  }

  return firstMessage(control.errors);
}

function firstMessage(errors: ValidationErrors): string | null {
  for (const key of Object.keys(errors)) {
    const builder = MESSAGES[key];
    if (builder) {
      return builder(errors[key]);
    }
  }
  return null;
}
