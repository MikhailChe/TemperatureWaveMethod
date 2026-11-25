import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Activity, FileText, FileUp, Play, Timer } from 'lucide-react';
import { lazy, Suspense, useState } from 'react';
import {
  get, measurementsPath, post, resultPath, runsPath,
  type Calibration, type Experiment, type FileResult, type Measurement, type Run, type RunDetails,
} from './api';
import { MeasurementForm } from './forms';
import { dateTime, decimal, Empty, Loading, Modal, Problem, useResource } from './ui';

const DiffusivityChart = lazy(() => import('./DiffusivityChart'));
const statusLabels: Record<Run['status'], string> = {
  QUEUED: 'В очереди',
  COMPLETED: 'Завершен',
  PARTIAL: 'С ошибками',
  FAILED: 'Не выполнен',
};

const scientific = new Intl.NumberFormat('ru-RU', { notation: 'scientific', maximumFractionDigits: 5 });
function Badge({ status }: { status: Run['status'] }) {
  const tone = { QUEUED: 'waiting', COMPLETED: 'success', PARTIAL: 'waiting', FAILED: 'failed' }[status];
  return <span className={'badge ' + tone}>{statusLabels[status]}</span>;
}

export function ExperimentPane({ experiment, calibration, calibrations, version }: {
  experiment: Experiment; calibration?: Calibration; calibrations: Calibration[]; version: number;
}) {
  const [upload, setUpload] = useState(false);
  const [runId, setRunId] = useState('');
  const cache = useQueryClient();
  const measurements = useResource<Measurement[]>(measurementsPath(experiment.id));
  const runPath = runsPath(experiment.id);
  const runs = useQuery({
    queryKey: [runPath],
    queryFn: ({ signal }) => get<Run[]>(runPath, signal),
    refetchInterval: ({ state }) => state.data?.some((item) => item.status === 'QUEUED') ? 1000 : false,
  });
  const run = runs.data?.find((item) => item.id === runId) ?? runs.data?.at(-1);
  const missing = experiment.channels.filter(({ column }) => !calibration?.channels.includes(column));

  const start = useMutation({
    mutationFn: (calibrationId: string) => post<Run>(runPath, { calibrationId }),
    onSuccess: (created) => {
      cache.setQueryData<Run[]>([runPath], (previous) => [
        ...(previous ?? []).filter((item) => item.id !== created.id), created,
      ]);
      setRunId(created.id);
      void cache.invalidateQueries({ queryKey: [runPath] });
    },
  });

  return <div className="experiment-content">
    <section className="surface detail" aria-label="Эксперимент">
      <div className="detail-heading">
        <div><span className="version-tag">Конфигурация v{version}</span><h2>{experiment.name}</h2></div>
        <button className="button secondary" type="button" onClick={() => setUpload(true)}>
          <FileUp size={17} /> Загрузить замеры
        </button>
      </div>
      <dl className="sample-data">
        <div><dt>Толщина образца</dt><dd>{decimal.format(experiment.lengthMeters * 1000)} мм</dd></div>
        <div><dt>Плотность</dt><dd>{decimal.format(experiment.density)} кг/м3</dd></div>
        <div><dt>Каналы</dt><dd>{experiment.channels.map(({ column, inverse }) => column + (inverse ? ' (инв.)' : '')).join(', ')}</dd></div>
      </dl>
      <div className="measurements-heading"><h3>Файлы замеров</h3>{measurements.data && <span className="count">{measurements.data.length}</span>}</div>
      {measurements.error ? <Problem error={measurements.error} retry={() => void measurements.refetch()} />
        : measurements.isPending ? <Loading /> : !measurements.data?.length
          ? <p className="measurements-empty"><FileText size={18} /> Файлы еще не загружены.</p>
          : <div className="table-wrap files-table"><table>
            <thead><tr><th>Файл</th><th>Время записи</th></tr></thead>
            <tbody>{measurements.data.map((file) => <tr key={file.id}>
              <td className="table-name">{file.fileName}</td><td>{dateTime.format(file.measuredAt)}</td>
            </tr>)}</tbody>
          </table></div>}
      <div className="compute-bar">
        <div>
          <strong>{calibration?.name ?? 'Выберите калибровку серии'}</strong>
          <p>{calibration && missing.length > 0 ? 'Нет поправок для каналов: ' + missing.map(({ column }) => column).join(', ')
            : calibration ? 'Фазовые поправки для этого запуска' : 'Загрузите или выберите поправки для этой версии установки.'}</p>
        </div>
        <button className="button primary" type="button"
          disabled={!calibration || !measurements.data?.length || missing.length > 0 || start.isPending}
          onClick={() => calibration && start.mutate(calibration.id)}><Play size={17} />
          {start.isPending ? 'Постановка в очередь...' : 'Запустить расчет'}
        </button>
      </div>
      {start.error && <Problem error={start.error} />}
    </section>

    <section className="surface results" aria-label="Результаты">
      <div className="panel-heading">
        <h2>Результаты</h2>
        {runs.data && runs.data.length > 0 && <label className="run-select">
          <span>Запуск</span><select aria-label="Запуск расчета" value={run?.id ?? ''} onChange={(event) => setRunId(event.target.value)}>
            {runs.data.slice().reverse().map((item) => <option value={item.id} key={item.id}>
              {dateTime.format(item.requestedAt)} / {statusLabels[item.status]}
            </option>)}
          </select>
        </label>}
      </div>
      {runs.error ? <Problem error={runs.error} retry={() => void runs.refetch()} />
        : runs.isPending ? <Loading />
          : run ? <Results key={run.id} run={run} calibrationName={calibrations.find(({ id }) => id === run.calibrationId)?.name} />
            : <Empty icon={<Activity size={26} />} title="Расчетов пока нет" description="После запуска здесь появятся результаты по файлам и каналам." />}
    </section>
    {upload && <Modal title="Загрузка замеров" close={() => setUpload(false)}>
      <MeasurementForm experiment={experiment} close={() => setUpload(false)}
        onUploaded={() => void cache.invalidateQueries({ queryKey: [measurementsPath(experiment.id)] })} />
    </Modal>}
  </div>;
}

function Results({ run, calibrationName }: { run: Run; calibrationName?: string }) {
  const path = resultPath(run.id);
  const details = useQuery({
    queryKey: [path],
    queryFn: ({ signal }) => get<RunDetails>(path, signal),
    refetchInterval: ({ state }) => !state.data || state.data.run.status === 'QUEUED' ? 1000 : false,
  });

  if (details.error) return <Problem error={details.error} retry={() => void details.refetch()} />;
  if (!details.data) return <Loading />;

  const { run: selectedRun, measurements } = details.data;
  return <>
    <div className="run-summary">
      <Badge status={selectedRun.status} />
      <span>{calibrationName ?? selectedRun.calibrationId}</span>
      <span>Файлы: {measurements.length}</span>
    </div>
    {selectedRun.status === 'QUEUED'
      ? <div className="loading queued" role="status"><Timer size={19} /> Результаты появятся после обработки файлов.</div>
      : <ResultsData measurements={measurements} />}
  </>;
}

function ResultsData({ measurements }: { measurements: FileResult[] }) {
  const successful = measurements.filter(({ result }) => result !== null);
  const errors = measurements.filter(({ error }) => error !== null);
  return <>
    {successful.length > 0 && <>
      <div className="chart-panel">
        <h3>Температуропроводность по времени замера</h3>
        <Suspense fallback={<Loading />}><DiffusivityChart measurements={successful} /></Suspense>
      </div>
      <div className="table-wrap results-table"><table>
        <thead><tr><th>Файл</th><th>Канал</th><th>Частота, Гц</th><th>Температуропроводность, м2/с</th>
          <th>Фаза, рад</th><th>Амплитуда</th><th>Каппа</th><th>Теплоемкость</th></tr></thead>
        <tbody>{successful.flatMap((file) => file.result?.channels.map((channel) => <tr key={file.measurementId + ':' + channel.column}>
          <td className="table-name">{file.fileName}</td><td>{channel.column}</td><td>{decimal.format(file.result!.frequencyHz)}</td>
          <td className="numeric result-number">{scientific.format(channel.diffusivity)}</td>
          <td className="numeric">{decimal.format(channel.phaseRadians)}</td><td className="numeric">{decimal.format(channel.amplitude)}</td>
          <td className="numeric">{decimal.format(channel.kappa)}</td><td className="numeric">{scientific.format(channel.capacitance)}</td>
        </tr>) ?? [])}</tbody>
      </table></div>
    </>}
    {errors.length > 0 && <div className="file-errors">
      <h3>Ошибки файлов</h3>
      <ul>{errors.map((file) => <li key={file.measurementId}><strong>{file.fileName}</strong><span>{file.error}</span></li>)}</ul>
    </div>}
  </>;
}
