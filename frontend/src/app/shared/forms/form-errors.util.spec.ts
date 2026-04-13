import { FormControl, Validators } from '@angular/forms';

import { getErrorMessage } from './form-errors.util';

describe('getErrorMessage', () => {
  it('returns null for a pristine untouched control', () => {
    const control = new FormControl('', [Validators.required]);
    expect(getErrorMessage(control)).toBeNull();
  });

  it('returns required message when control is touched and empty', () => {
    const control = new FormControl('', [Validators.required]);
    control.markAsTouched();
    expect(getErrorMessage(control)).toBe('Este campo es obligatorio.');
  });

  it('returns minlength message with the required length', () => {
    const control = new FormControl('ab', [Validators.minLength(4)]);
    control.markAsTouched();
    expect(getErrorMessage(control)).toBe('Debe tener al menos 4 caracteres.');
  });

  it('returns min message with the required minimum', () => {
    const control = new FormControl(-1, [Validators.min(0)]);
    control.markAsTouched();
    expect(getErrorMessage(control)).toBe('El valor mínimo es 0.');
  });

  it('returns max message with the required maximum', () => {
    const control = new FormControl(200, [Validators.max(120)]);
    control.markAsTouched();
    expect(getErrorMessage(control)).toBe('El valor máximo es 120.');
  });

  it('returns server-provided message when control has a "server" error', () => {
    const control = new FormControl('x');
    control.setErrors({ server: 'Identificación duplicada' });
    control.markAsTouched();
    expect(getErrorMessage(control)).toBe('Identificación duplicada');
  });

  it('returns null when control is null', () => {
    expect(getErrorMessage(null)).toBeNull();
  });
});
