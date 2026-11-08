export function formatBytes(bytes: number): string {
  if (bytes <= 0 || isNaN(bytes)) return "0 B";

  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  const unitIndex = Math.min(i, sizes.length - 1);
  const value = bytes / Math.pow(k, unitIndex);

  if (unitIndex === 0) {
    return `${bytes} B`;
  }

  const formatted = value.toFixed(1);
  const display = formatted.endsWith(".0") && value >= 100 ? Math.round(value).toString() : formatted;

  return `${display} ${sizes[unitIndex]}`;
}

export function formatDate(date: Date | string): string {
  const d = date instanceof Date ? date : new Date(date);
  if (isNaN(d.getTime())) return "Fecha inválida";

  const pad = (n: number) => n.toString().padStart(2, "0");
  const day = pad(d.getDate());
  const monthNames = [
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic",
  ];
  const month = monthNames[d.getMonth()];
  const year = d.getFullYear();
  const hours = pad(d.getHours());
  const minutes = pad(d.getMinutes());

  return `${day} ${month} ${year}, ${hours}:${minutes}`;
}

