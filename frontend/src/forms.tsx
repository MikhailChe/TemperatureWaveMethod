import { FileUp, Plus, Trash2 } from 'lucide-react';
import { useRef, useState, type FormEvent, type ReactNode } from 'react';
import {
  calibrationsPath, configurationsPath, experimentsPath, installationsPath, measurementsPath, post,
  type Calibration, type Channel, type Configuration, type Experiment, type Installation,
} from './api';
import { localDateTime, Problem } from './ui';

type FormProps<T> = { done: (value: T) => void; close: () => void };

function ActionForm({ children, label, close, submit, disabled = false }: {
  children: ReactNode; label: string; close: () => void;
  submit: (values: FormData) => Promise<void>; disabled?: boolean;
}) {
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<unknown>();
  const inFlight = useRef(false);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (inFlight.current) return;
    const values = new FormData(event.currentTarget);
    inFlight.current = true;
    setPending(true);
    setError(undefined);
    try {
      await submit(values);
    } catch (failure) {
      setError(failure);
    } finally {
      inFlight.current = false;
      setPending(false);
    }
  }

  return <form onSubmit={onSubmit}>
    <fieldset disabled={pending} className="form-fields">{children}</fieldset>
    {error instanceof Error && <Problem error={error} />}
    <div className="form-actions">
      <button type="button" className="button secondary" onClick={close} disabled={pending}>Отмена</button>
      <button type="submit" className="button primary" disabled={pending || disabled}>
        {pending ? 'Сохранение...' : label}
      </button>
    </div>
  </form>;
}

const text = (data: FormData, key: string) => String(data.get(key) ?? '').trim();

export function InstallationForm({ done, close }: FormProps<Installation>) {
  return <ActionForm close={close} label="Добавить установку" submit={async (values) => {
    done(await post<Installation>(installationsPath, { name: text(values, 'name') }));
  }}>
    <label>Название установки<input name="name" required maxLength={200} placeholder="Например, Оптический стенд" /></label>
  </ActionForm>;
}

const configurationFields = [
  { key: 'laser', label: 'Лазер', placeholder: 'Модель или положение' },
  { key: 'chopper', label: 'Обтюратор', placeholder: 'Модель или положение' },
  { key: 'amplifier', label: 'Тракт усиления', placeholder: 'Тракт и настройки по каналам' },
  { key: 'lowPassHz', label: 'ФНЧ, Гц', placeholder: '100', numeric: true },
  { key: 'highPassHz', label: 'ФВЧ, Гц', placeholder: '0,1', numeric: true },
  { key: 'gain', label: 'Коэффициент усиления', placeholder: '270', numeric: true },
] as const;

export const settingLabels: Record<string, string> = {
  ...Object.fromEntries(configurationFields.map(({ key, label }) => [key, label])),
  notes: 'Что изменилось',
};

export function ConfigurationForm({ installation, previous, done, close }: FormProps<Configuration> & {
  installation: Installation; previous?: Configuration;
}) {
  return <ActionForm close={close} label="Создать версию" submit={async (values) => {
    const settings = { ...previous?.settings };
    for (const field of configurationFields) {
      const value = text(values, field.key);
      if (value) settings[field.key] = 'numeric' in field ? Number(value) : value;
      else delete settings[field.key];
    }
    const notes = text(values, 'notes');
    if (notes) settings.notes = notes;
    else delete settings.notes;
    done(await post<Configuration>(configurationsPath(installation.id), {
      effectiveFrom: new Date(text(values, 'effectiveFrom')).getTime(), settings,
    }));
  }}>
    <label>Действует с<input type="datetime-local" step="0.001" name="effectiveFrom" required defaultValue={localDateTime(Date.now())} /></label>
    <div className="form-grid">
      {configurationFields.map((field) => {
        const value = previous?.settings[field.key];
        return <label key={field.key}>{field.label}<input name={field.key}
          type={'numeric' in field ? 'number' : 'text'} min={'numeric' in field ? 0 : undefined}
          step={'numeric' in field ? 'any' : undefined} maxLength={200}
          defaultValue={typeof value === 'number' || typeof value === 'string' ? value : ''}
          placeholder={field.placeholder} /></label>;
      })}
    </div>
    <label>Что изменилось<textarea name="notes" rows={3} maxLength={2000} placeholder="Например, заменен усилитель на канале 2" /></label>
  </ActionForm>;
}

type ChannelRow = Channel & { id: number };

function ChannelFields({ rows, calibration, changed }: {
  rows: ChannelRow[]; calibration?: boolean; changed: (rows: ChannelRow[]) => void;
}) {
  const nextId = useRef(rows.length);

  function change(id: number, value: Partial<Channel>) {
    changed(rows.map((row) => row.id === id ? { ...row, ...value } : row));
  }

  return <>
    <div className="channel-fields">
      {rows.map(({ id, column, inverse }, index) => <div className="field-row" key={id}>
        <label>Канал<input type="number" min={1} max={64} required value={column || ''}
          aria-label={(calibration ? 'Канал калибровки ' : 'Канал эксперимента ') + (index + 1)}
          onChange={(event) => change(id, { column: Number(event.target.value) })} /></label>
        {calibration ? <label>Файл поправок<input type="file" name={'file-' + id} required
          aria-label={'Файл поправок ' + (index + 1)} /></label> :
          <label className="check-label"><input type="checkbox" checked={inverse}
            onChange={(event) => change(id, { inverse: event.target.checked })} /> Инверсия сигнала</label>}
        <button type="button" className="icon-button remove-row" aria-label="Удалить канал" disabled={rows.length === 1}
          onClick={() => changed(rows.filter((row) => row.id !== id))}><Trash2 size={17} /></button>
      </div>)}
    </div>
    <button type="button" className="button secondary compact" disabled={rows.length >= 64}
      onClick={() => {
        const column = Array.from({ length: 64 }, (_, index) => index + 1)
          .find((number) => !rows.some((row) => row.column === number));
        if (column !== undefined) changed([...rows, { id: nextId.current++, column, inverse: false }]);
      }}><Plus size={16} /> Добавить канал</button>
  </>;
}

function checkColumns(channels: Channel[]) {
  if (new Set(channels.map(({ column }) => column)).size !== channels.length) {
    throw new Error('Укажите разные номера каналов.');
  }
}

export function CalibrationForm({ configuration, done, close }: FormProps<Calibration> & { configuration: Configuration }) {
  const [channels, setChannels] = useState<ChannelRow[]>([{ column: 1, inverse: false, id: 0 }]);
  return <ActionForm close={close} label="Загрузить калибровку" submit={async (values) => {
    checkColumns(channels);
    const upload = new FormData();
    upload.append('name', text(values, 'name'));
    upload.append('numberLocale', text(values, 'numberLocale'));
    for (const channel of channels) {
      const file = values.get('file-' + channel.id);
      if (!(file instanceof File) || !file.size) throw new Error('Выберите файл для каждого канала.');
      checkSize(file);
      upload.append('channel', String(channel.column));
      upload.append('file', file);
    }
    done(await post<Calibration>(calibrationsPath(configuration.id), upload));
  }}>
    <p className="form-intro">Калибровка будет доступна для всех экспериментов версии v{configuration.version}.</p>
    <div className="form-grid">
      <label>Название калибровки<input name="name" required maxLength={200} placeholder="Например, Юстировка после замены ФНЧ" /></label>
      <label>Десятичный разделитель<select name="numberLocale" defaultValue="ru-RU">
        <option value="ru-RU">Запятая (ru-RU)</option>
        <option value="en-US">Точка (en-US)</option>
      </select></label>
    </div>
    <ChannelFields calibration rows={channels} changed={setChannels} />
  </ActionForm>;
}

export function ExperimentForm({ configuration, calibration, done, close }: FormProps<Experiment> & {
  configuration: Configuration; calibration?: Calibration;
}) {
  const [channels, setChannels] = useState<ChannelRow[]>(() =>
    (calibration?.channels ?? [1]).map((column, id) => ({ column, inverse: false, id })));
  return <ActionForm close={close} label="Создать эксперимент" submit={async (values) => {
    checkColumns(channels);
    const length = Number(text(values, 'length'));
    const density = Number(text(values, 'density'));
    if (length <= 0 || density <= 0) throw new Error('Толщина и плотность должны быть положительными.');
    done(await post<Experiment>(experimentsPath(configuration.id), {
      name: text(values, 'name'), lengthMeters: length / 1000, density,
      channels: channels.map(({ column, inverse }) => ({ column, inverse })),
    }));
  }}>
    <label>Название эксперимента<input name="name" required maxLength={200} placeholder="Например, Образец A" /></label>
    <div className="form-grid">
      <label>Толщина образца, мм<input name="length" type="number" min={0} step="any" required placeholder="2" /></label>
      <label>Плотность, кг/м3<input name="density" type="number" min={0} step="any" required placeholder="8000" /></label>
    </div>
    <ChannelFields rows={channels} changed={setChannels} />
  </ActionForm>;
}

type Upload = { file: File; date: string; completed: boolean };

function checkSize(file: File) {
  if (file.size > 16 * 1024 * 1024) throw new Error('Файл ' + file.name + ' превышает 16 МБ.');
}

export function MeasurementForm({ experiment, onUploaded, close }: {
  experiment: Experiment; onUploaded: () => void; close: () => void;
}) {
  const [uploads, setUploads] = useState<Upload[]>([]);
  return <ActionForm close={close} label="Загрузить замеры" disabled={!uploads.some(({ completed }) => !completed)}
    submit={async () => {
      const pending = uploads.map((upload, index) => ({ ...upload, index })).filter(({ completed }) => !completed);
      for (const { file, date } of pending) {
        checkSize(file);
        if (!file.size) throw new Error('Файл ' + file.name + ' пуст.');
        if (!Number.isFinite(new Date(date).getTime())) throw new Error('Укажите время замера для ' + file.name + '.');
      }
      for (const { file, date, index } of pending) {
        const payload = new FormData();
        payload.append('file', file);
        payload.append('measuredAt', String(new Date(date).getTime()));
        try {
          await post(measurementsPath(experiment.id), payload);
        } catch (error) {
          throw new Error(file.name + ': ' + (error instanceof Error ? error.message : 'Ошибка загрузки.'));
        }
        setUploads((current) => current.map((entry, i) => i === index ? { ...entry, completed: true } : entry));
        onUploaded();
      }
      close();
    }}>
    <label className="upload-target"><FileUp size={25} /><strong>Выберите файлы замеров</strong><span>До 16 МБ на файл</span>
      <input aria-label="Файлы замеров" type="file" multiple onChange={(event) => {
        setUploads(Array.from(event.target.files ?? [], (file) => ({
          file, date: localDateTime(file.lastModified), completed: false,
        })));
      }} />
    </label>
    {uploads.length > 0 && <p className="form-intro">По умолчанию указано время изменения файлов. При необходимости укажите время записи.</p>}
    {uploads.map(({ file, date, completed }, index) => <div className="upload-file" key={index}>
      <span title={file.name}>{file.name}</span>
      {completed ? <span className="badge success">Загружен</span> : <label>Время замера
        <input type="datetime-local" step="0.001" required value={date} aria-label={'Время замера ' + (index + 1)}
          onChange={(event) => {
            const value = event.target.value;
            setUploads((current) => current.map((entry, i) => i === index ? { ...entry, date: value } : entry));
          }} /></label>}
    </div>)}
  </ActionForm>;
}
