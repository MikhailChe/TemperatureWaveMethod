import { useQuery } from '@tanstack/react-query';
import { AlertCircle, LoaderCircle, X } from 'lucide-react';
import { useEffect, useRef, type ReactNode } from 'react';
import { get } from './api';

export const dateTime = new Intl.DateTimeFormat('ru-RU', {
  dateStyle: 'medium',
  timeStyle: 'medium',
});
export const decimal = new Intl.NumberFormat('ru-RU', { maximumFractionDigits: 5 });

export function readPreference(key: string) {
  try {
    return localStorage.getItem('twm.' + key) || '';
  } catch {
    return '';
  }
}

export function savePreference(key: string, value: string) {
  try {
    if (value) localStorage.setItem('twm.' + key, value);
    else localStorage.removeItem('twm.' + key);
  } catch {
    // Storage is optional in private or restricted browser sessions.
  }
}

export function localDateTime(millis: number) {
  const date = new Date(millis);
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
  return date.toISOString().slice(0, -1);
}

export function useResource<T>(path: string, enabled = true) {
  return useQuery({
    queryKey: [path],
    queryFn: ({ signal }) => get<T>(path, signal),
    enabled,
  });
}

export function Problem({ error, retry }: { error: unknown; retry?: () => void }) {
  return <div className="problem" role="alert">
    <AlertCircle size={18} />
    <span>{error instanceof Error ? error.message : 'Произошла ошибка.'}</span>
    {retry && <button type="button" className="text-button" onClick={retry}>Повторить</button>}
  </div>;
}

export function Loading() {
  return <div className="loading" role="status"><LoaderCircle size={17} /> Загрузка</div>;
}

export function Empty({
  icon, title, description, action,
}: { icon: ReactNode; title: string; description?: string; action?: ReactNode }) {
  return <div className="empty">
    <div className="empty-icon">{icon}</div>
    <h3>{title}</h3>
    {description && <p>{description}</p>}
    {action}
  </div>;
}

export function Modal({ title, close, children }: { title: string; close: () => void; children: ReactNode }) {
  const dialog = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const element = dialog.current;
    element?.showModal();
    return () => element?.close();
  }, []);
  return <dialog ref={dialog} aria-labelledby="dialog-title" onCancel={(event) => { event.preventDefault(); close(); }}>
    <div className="modal-heading">
      <h2 id="dialog-title">{title}</h2>
      <button type="button" className="icon-button" aria-label="Закрыть" onClick={close}><X size={22} /></button>
    </div>
    {children}
  </dialog>;
}
