import { useEffect, useRef } from 'react';
import { init, use, type ComposeOption } from 'echarts/core';
import { LineChart, type LineSeriesOption } from 'echarts/charts';
import { GridComponent, LegendComponent, TooltipComponent,
  type GridComponentOption, type LegendComponentOption, type TooltipComponentOption } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import type { FileResult } from './api';

use([LineChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer]);

type ChartOption = ComposeOption<LineSeriesOption | GridComponentOption | LegendComponentOption | TooltipComponentOption>;

export default function DiffusivityChart({ measurements }: { measurements: FileResult[] }) {
  const container = useRef<HTMLDivElement>(null);
  useEffect(() => {
    const element = container.current;
    if (!element) return;
    const chart = init(element);
    const values = new Map<number, [number, number][]>();
    for (const measurement of measurements) {
      for (const channel of measurement.result?.channels ?? []) {
        const points = values.get(channel.column) ?? [];
        points.push([measurement.measuredAt, channel.diffusivity]);
        values.set(channel.column, points);
      }
    }
    const option: ChartOption = {
      color: ['#007f72', '#be633d', '#4a66b7', '#a4508b', '#8c731f'],
      animation: false,
      grid: { left: 72, right: 23, bottom: 40, top: 48 },
      legend: { top: 5, type: 'scroll', textStyle: { color: '#48635f' } },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'time', axisLabel: { color: '#48635f', hideOverlap: true } },
      yAxis: {
        type: 'value',
        name: 'м2/с',
        nameTextStyle: { color: '#48635f' },
        axisLabel: { color: '#48635f', formatter: (value: number) => value.toExponential(1) },
        splitLine: { lineStyle: { color: '#e0eae7' } },
      },
      series: [...values].sort(([a], [b]) => a - b).map(([channel, data]) => ({
        name: 'Канал ' + channel, type: 'line', data, symbolSize: 7, lineStyle: { width: 2 },
      })),
    };
    chart.setOption(option);
    const observer = new ResizeObserver(() => chart.resize());
    observer.observe(element);
    return () => { observer.disconnect(); chart.dispose(); };
  }, [measurements]);
  return <div className="chart" ref={container} role="img" aria-label="График температуропроводности по каналам" />;
}
