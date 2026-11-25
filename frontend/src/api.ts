export type Installation = { id: string; name: string };
export type Configuration = {
  id: string;
  installationId: string;
  version: number;
  effectiveFrom: number;
  settings: Record<string, unknown>;
};
export type Calibration = {
  id: string;
  configurationId: string;
  name: string;
  numberLocale: string;
  channels: number[];
};
export type Channel = { column: number; inverse: boolean };
export type Experiment = {
  id: string;
  configurationId: string;
  name: string;
  lengthMeters: number;
  density: number;
  channels: Channel[];
};
export type Measurement = { id: string; experimentId: string; measuredAt: number; fileName: string };
export type Run = {
  id: string;
  experimentId: string;
  configurationId: string;
  calibrationId: string;
  requestedAt: number;
  status: 'QUEUED' | 'COMPLETED' | 'PARTIAL' | 'FAILED';
};
export type ChannelResult = {
  column: number;
  amplitude: number;
  phaseRadians: number;
  kappa: number;
  diffusivity: number;
  capacitance: number;
};
export type FileResult = {
  measurementId: string;
  fileName: string;
  measuredAt: number;
  result: { frequencyHz: number; channels: ChannelResult[] } | null;
  error: string | null;
};
export type RunDetails = { run: Run; measurements: FileResult[] };

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch('/api' + path, init);
  } catch (error) {
    if (init?.signal?.aborted) throw error;
    throw new Error('Не удалось подключиться к серверу.');
  }
  const isJson = response.headers.get('content-type')?.includes('json');
  if (!response.ok) {
    const body = isJson ? await response.json().catch(() => null) : null;
    if (response.status === 413) throw new Error('Файл или пакет превышает лимит загрузки.');
    if (response.status >= 500) throw new Error('Сервер недоступен. Проверьте подключение и повторите запрос.');
    throw new Error(body?.detail || body?.message || `Ошибка запроса (HTTP ${response.status}).`);
  }
  if (!isJson) throw new Error('Сервер вернул ответ в неожиданном формате.');
  return response.json();
}

export function get<T>(path: string, signal?: AbortSignal) {
  return request<T>(path, { signal });
}

export function post<T>(path: string, data: object) {
  return request<T>(path, data instanceof FormData
    ? { method: 'POST', body: data }
    : { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
}

export const installationsPath = '/installations';
export const configurationsPath = (id: string) => `/installations/${id}/configurations`;
export const calibrationsPath = (id: string) => `/configurations/${id}/calibrations`;
export const experimentsPath = (id: string) => `/configurations/${id}/experiments`;
export const measurementsPath = (id: string) => `/experiments/${id}/measurements`;
export const runsPath = (id: string) => `/experiments/${id}/runs`;
export const resultPath = (id: string) => `/runs/${id}`;
