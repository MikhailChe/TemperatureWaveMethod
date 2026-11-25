import { useQueryClient } from '@tanstack/react-query';
import {
  Activity, Beaker, Boxes, ChevronRight, CircuitBoard, FileUp, FlaskConical, Layers3, Plus, Radio, SlidersHorizontal, Waves,
} from 'lucide-react';
import { useState } from 'react';
import {
  calibrationsPath, configurationsPath, experimentsPath, installationsPath,
  type Calibration, type Configuration, type Experiment, type Installation,
} from './api';
import { CalibrationForm, ConfigurationForm, ExperimentForm, InstallationForm, settingLabels } from './forms';
import { ExperimentPane } from './ExperimentPane';
import { dateTime, decimal, Empty, Loading, Modal, Problem, readPreference, savePreference, useResource } from './ui';

type View = 'experiments' | 'calibrations' | 'configurations';
type Form = 'installation' | 'configuration' | 'calibration' | 'experiment';
const views = [
  { id: 'experiments', label: 'Эксперименты', icon: FlaskConical },
  { id: 'calibrations', label: 'Калибровки', icon: Activity },
  { id: 'configurations', label: 'Конфигурации', icon: Layers3 },
] as const;

export function App() {
  const cache = useQueryClient();
  const [view, setView] = useState<View>('experiments');
  const [form, setForm] = useState<Form>();
  const [installationId, setInstallationId] = useState(() => readPreference('installation'));
  const [configurationId, setConfigurationId] = useState(() => readPreference('configuration'));
  const [experimentId, setExperimentId] = useState('');
  const [calibrationChoices, setCalibrationChoices] = useState<Record<string, string>>({});
  const close = () => setForm(undefined);
  const installations = useResource<Installation[]>(installationsPath);
  const installation = installations.data?.find((item) => item.id === installationId) ?? installations.data?.[0];
  const configurations = useResource<Configuration[]>(configurationsPath(installation?.id ?? ''), !!installation);
  const configuration = configurations.data?.find((item) => item.id === configurationId) ?? configurations.data?.at(-1);
  const calibrations = useResource<Calibration[]>(calibrationsPath(configuration?.id ?? ''), !!configuration);
  const experiments = useResource<Experiment[]>(experimentsPath(configuration?.id ?? ''), !!configuration);
  const experiment = experiments.data?.find((item) => item.id === experimentId) ?? experiments.data?.[0];
  const preference = configuration ? 'calibration.' + configuration.id : '';
  const selectedCalibrationId = calibrationChoices[preference] ?? readPreference(preference);
  const calibration = calibrations.data?.find((item) => item.id === selectedCalibrationId);

  function chooseInstallation(id: string) {
    setInstallationId(id);
    setConfigurationId('');
    setExperimentId('');
    savePreference('installation', id);
    savePreference('configuration', '');
  }

  function chooseConfiguration(id: string) {
    setConfigurationId(id);
    setExperimentId('');
    savePreference('configuration', id);
  }

  function chooseCalibration(id: string) {
    setCalibrationChoices((current) => ({ ...current, [preference]: id }));
    savePreference(preference, id);
  }

  function remember<T extends { id: string }>(path: string, item: T) {
    cache.setQueryData<T[]>([path], (items) => [...(items ?? []).filter(({ id }) => id !== item.id), item]);
    void cache.invalidateQueries({ queryKey: [path] });
  }

  const loadingScope = installations.isPending || (!!installation && configurations.isPending);
  const scopeError = installations.error || (installation && configurations.error);
  const sectionLabel = views.find(({ id }) => id === view)?.label;

  return <div className="app-shell">
    <aside className="sidebar">
      <a className="brand" href="./" aria-label="Тепловая волна, главная">
        <span className="brand-icon"><Waves size={26} strokeWidth={2.15} /></span>
        <span><strong>Тепловая волна</strong><small>Лабораторный журнал</small></span>
      </a>
      <nav aria-label="Разделы">
        {views.map(({ id, label, icon: Icon }) => <button key={id} type="button"
          className={'nav-button ' + (view === id ? 'active' : '')} aria-current={view === id ? 'page' : undefined}
          onClick={() => setView(id)}><Icon size={19} /><span>{label}</span></button>)}
      </nav>
      <div className="sidebar-bottom"><span className="sidebar-mark"><CircuitBoard size={19} /></span><span>Метод температурной волны</span></div>
    </aside>

    <div className="main-shell">
      <div className="top-bar">
        <div className="breadcrumb"><span>Лаборатория</span><ChevronRight size={15} /><strong>{sectionLabel}</strong></div>
        {installation && <div className="top-installation"><Radio size={15} /><span>{installation.name}</span></div>}
      </div>
      <main>
        <header className="page-heading">
          <div>
            <h1>{sectionLabel}</h1>
            <p>{view === 'experiments' ? 'Замеры, расчеты и результаты для выбранной конфигурации.' :
              view === 'calibrations' ? 'Фазовые поправки, доступные всей серии экспериментов.' :
              'История изменений лазера, обтюратора и трактов усиления.'}</p>
          </div>
          {view === 'experiments' && configuration && <button className="button primary" type="button" onClick={() => setForm('experiment')}>
            <Plus size={18} /> Новый эксперимент
          </button>}
          {view === 'calibrations' && configuration && <button className="button primary" type="button" onClick={() => setForm('calibration')}>
            <FileUp size={18} /> Загрузить калибровку
          </button>}
          {view === 'configurations' && installation && <button className="button primary" type="button" onClick={() => setForm('configuration')}>
            <Plus size={18} /> Новая версия
          </button>}
        </header>

        <section className="scope surface" aria-label="Рабочая конфигурация">
          <div className="scope-icon"><SlidersHorizontal size={22} /></div>
          <label>Установка<select value={installation?.id ?? ''} disabled={!installations.data?.length}
            onChange={(event) => chooseInstallation(event.target.value)}>
            {!installations.data?.length && <option value="">Выберите установку</option>}
            {installations.data?.map((item) => <option value={item.id} key={item.id}>{item.name}</option>)}
          </select></label>
          <button className="icon-button add-scope" type="button" onClick={() => setForm('installation')}
            aria-label="Добавить установку" title="Добавить установку"><Plus size={20} /></button>
          <div className="scope-divider" />
          <label>Версия конфигурации<select value={configuration?.id ?? ''} disabled={!configurations.data?.length}
            onChange={(event) => chooseConfiguration(event.target.value)}>
            {!configurations.data?.length && <option value="">Выберите версию</option>}
            {configurations.data?.slice().reverse().map((item) => <option value={item.id} key={item.id}>
              v{item.version} / {dateTime.format(item.effectiveFrom)}
            </option>)}
          </select></label>
          <button className="icon-button add-scope" type="button" onClick={() => setForm('configuration')}
            disabled={!installation} aria-label="Добавить версию" title="Добавить версию"><Plus size={20} /></button>
        </section>

        {scopeError ? <Problem error={scopeError} retry={() => {
          if (installations.error) void installations.refetch();
          else void configurations.refetch();
        }} /> : loadingScope ? <Loading /> : !configuration ? <section className="surface">
          <Empty icon={installation ? <Layers3 size={28} /> : <CircuitBoard size={28} />}
            title={installation ? 'Создайте первую версию' : 'Добавьте установку'}
            description={installation ? 'Зафиксируйте параметры оборудования перед загрузкой калибровки и замеров.' :
              'Начните с установки, на которой записываются замеры.'}
            action={<button type="button" className="button primary" onClick={() => setForm(installation ? 'configuration' : 'installation')}>
              <Plus size={18} /> {installation ? 'Создать версию' : 'Добавить установку'}
            </button>} />
        </section> : <>
          {view !== 'configurations' && <section className="series-band" aria-label="Калибровка серии">
            <div className="series-icon"><Activity size={23} /></div>
            <div className="series-info">
              <h2>Калибровка серии</h2>
              <p>Выбор сохраняется для экспериментов версии v{configuration.version} в этом браузере.</p>
            </div>
            <div className="series-control">
              <select aria-label="Калибровка серии" value={calibration?.id ?? ''} disabled={!calibrations.data?.length}
                onChange={(event) => chooseCalibration(event.target.value)}>
                <option value="">{calibrations.isPending ? 'Загрузка...' : 'Выберите калибровку'}</option>
                {calibrations.data?.map((item) => <option value={item.id} key={item.id}>{item.name}</option>)}
              </select>
              <button type="button" className="icon-button" aria-label="Добавить калибровку" title="Добавить калибровку"
                onClick={() => setForm('calibration')}><Plus size={20} /></button>
            </div>
            {calibrations.error && <Problem error={calibrations.error} retry={() => void calibrations.refetch()} />}
          </section>}

          {view === 'experiments' && (experiments.error
            ? <Problem error={experiments.error} retry={() => void experiments.refetch()} />
            : experiments.isPending ? <Loading /> : !experiment ? <section className="surface">
              <Empty icon={<Beaker size={30} />} title="Нет экспериментов"
                description="Добавьте образец и каналы, затем загрузите файлы замеров."
                action={<button className="button primary" type="button" onClick={() => setForm('experiment')}>
                  <Plus size={17} /> Новый эксперимент
                </button>} />
            </section> : <div className="experiment-layout">
              <section className="surface experiment-list" aria-label="Список экспериментов">
                <div className="panel-heading"><h2>Эксперименты</h2><span className="count">{experiments.data?.length}</span></div>
                <ul>{experiments.data?.map((item) => <li key={item.id}>
                  <button className={'experiment-option ' + (item.id === experiment.id ? 'selected' : '')} type="button"
                    aria-pressed={item.id === experiment.id} onClick={() => setExperimentId(item.id)}>
                    <span className="list-icon"><FlaskConical size={18} /></span>
                    <span className="option-copy"><strong>{item.name}</strong><small>{decimal.format(item.lengthMeters * 1000)} мм</small></span>
                  </button>
                </li>)}</ul>
              </section>
              <ExperimentPane key={experiment.id} experiment={experiment} calibration={calibration}
                calibrations={calibrations.data ?? []} version={configuration.version} />
            </div>)}

          {view === 'calibrations' && !calibrations.error && (calibrations.isPending ? <Loading /> : <section className="surface">
            {calibrations.data?.length ? <div className="table-wrap"><table><thead><tr>
              <th>Название</th><th>Каналы</th><th>Формат чисел</th><th>Выбор для серии</th>
            </tr></thead><tbody>{calibrations.data.map((item) => <tr key={item.id}>
              <td className="table-name">{item.name}</td><td>{item.channels.join(', ')}</td><td>{item.numberLocale}</td>
              <td>{item.id === calibration?.id ? <span className="badge success">Выбрана</span> :
                <button type="button" className="button secondary compact" onClick={() => chooseCalibration(item.id)}>Выбрать</button>}</td>
            </tr>)}</tbody></table></div> : <Empty icon={<Activity size={28} />} title="Требуется калибровка"
              description={'Загрузите готовые фазовые поправки для версии v' + configuration.version + '.'}
              action={<button type="button" className="button primary" onClick={() => setForm('calibration')}><FileUp size={17} /> Загрузить калибровку</button>} />}
          </section>)}

          {view === 'configurations' && <div className="experiment-layout">
            <section className="surface experiment-list" aria-label="История версий">
              <div className="panel-heading"><h2>История версий</h2><span className="count">{configurations.data?.length}</span></div>
              <ul>{configurations.data?.slice().reverse().map((item) => <li key={item.id}>
                <button className={'experiment-option ' + (item.id === configuration.id ? 'selected' : '')}
                  aria-pressed={item.id === configuration.id} type="button" onClick={() => chooseConfiguration(item.id)}>
                  <span className="list-icon"><Layers3 size={18} /></span>
                  <span className="option-copy"><strong>Версия v{item.version}</strong><small>{dateTime.format(item.effectiveFrom)}</small></span>
                </button>
              </li>)}</ul>
            </section>
            <section className="surface version-details">
              <div className="panel-heading"><div><h2>Версия v{configuration.version}</h2><p>Действует с {dateTime.format(configuration.effectiveFrom)}</p></div><Boxes size={23} /></div>
              {Object.keys(configuration.settings).length > 0 ? <dl className="settings">
                {Object.entries(configuration.settings).map(([key, value]) => <div key={key}>
                  <dt>{settingLabels[key] ?? key}</dt>
                  <dd>{typeof value === 'object' ? JSON.stringify(value) : String(value)}</dd>
                </div>)}
              </dl> : <p className="muted version-empty">Параметры оборудования не указаны.</p>}
            </section>
          </div>}
        </>}
      </main>
    </div>

    {form === 'installation' && <Modal title="Новая установка" close={close}>
      <InstallationForm close={close} done={(item) => { remember(installationsPath, item); chooseInstallation(item.id); close(); }} />
    </Modal>}
    {form === 'configuration' && installation && <Modal title="Новая версия конфигурации" close={close}>
      <ConfigurationForm installation={installation} previous={configuration} close={close} done={(item) => {
        remember(configurationsPath(installation.id), item); chooseConfiguration(item.id); close();
      }} />
    </Modal>}
    {form === 'calibration' && configuration && <Modal title="Загрузка калибровки" close={close}>
      <CalibrationForm configuration={configuration} close={close} done={(item) => {
        remember(calibrationsPath(configuration.id), item);
        if (!calibration) chooseCalibration(item.id);
        close();
      }} />
    </Modal>}
    {form === 'experiment' && configuration && <Modal title="Новый эксперимент" close={close}>
      <ExperimentForm configuration={configuration} calibration={calibration} close={close} done={(item) => {
        remember(experimentsPath(configuration.id), item); setExperimentId(item.id); close();
      }} />
    </Modal>}
  </div>;
}
