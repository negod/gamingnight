export function roundResultValue(value: number): number {
  return Math.round(value * 1000) / 1000;
}

export function formatResultValue(value: number): string {
  return roundResultValue(value).toLocaleString('en-US', {
    maximumFractionDigits: 3,
    useGrouping: false,
  });
}
