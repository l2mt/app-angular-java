export type Genero = 'MASCULINO' | 'FEMENINO' | 'OTRO';

export interface Cliente {
  clienteId: number;
  nombre: string;
  genero: Genero;
  edad: number;
  identificacion: string;
  direccion: string | null;
  telefono: string | null;
  estado: boolean;
}

export interface ClienteCreatePayload {
  nombre: string;
  genero: Genero;
  edad: number;
  identificacion: string;
  direccion?: string;
  telefono?: string;
  contrasena: string;
  estado: boolean;
}

export type ClienteUpdatePayload = Partial<ClienteCreatePayload>;
