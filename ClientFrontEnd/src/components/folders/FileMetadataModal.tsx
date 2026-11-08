import { useEffect, useState } from "react";
import "../../styles/folder/FileMetadataModal.css";
import { getFileMetadata } from "../../services/fileService";
import type { FileVariant, MetadataFileType } from "../../types/fileType";
import { formatBytes, formatDate } from "../../utils/formatters";

interface FileMetadataModalProps {
  fileId: string;
  fallbackFileName?: string;
  fallbackFileType?: FileVariant;
  closeMethod: () => void;
}

export function FileMetadataModal({
  fileId,
  fallbackFileName,
  fallbackFileType,
  closeMethod,
}: FileMetadataModalProps) {
  const [metadata, setMetadata] = useState<MetadataFileType | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    let active = true;

    async function fetchMetadata() {
      setIsLoading(true);
      setError("");

      try {
        const data = await getFileMetadata(fileId);
        if (active) {
          setMetadata(data);
        }
      } catch (err: unknown) {
        if (active) {
          setError(
            err instanceof Error
              ? err.message
              : "No se pudieron obtener los metadatos del archivo.",
          );
        }
      } finally {
        if (active) {
          setIsLoading(false);
        }
      }
    }

    fetchMetadata();
    return () => {
      active = false;
    };
  }, [fileId]);

  function handleCopyUuid(uuid: string) {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(uuid);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  }

  const displayName = metadata?.fileName ?? fallbackFileName ?? "Archivo";
  const displayType = metadata?.fileTypeName ?? fallbackFileType ?? "doc";

  return (
    <div className="metadata-modal-overlay">
      <div className="metadata-modal-content">
        <div className="metadata-modal__header">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            className="metadata-modal__svg"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="16" x2="12" y2="12" />
            <line x1="12" y1="8" x2="12.01" y2="8" />
          </svg>
          <div className="metadata-modal__titles">
            <h2>Metadatos Técnicos</h2>
            <p className="metadata-modal__filename">{displayName}</p>
          </div>
        </div>

        {isLoading && (
          <div className="metadata-modal__loading">
            <div className="metadata-modal__spinner" />
            <p>Consultando especificaciones en el clúster DFSha...</p>
          </div>
        )}

        {error && (
          <div className="metadata-modal__error-box">
            <p className="metadata-modal__error-text">{error}</p>
            <button
              type="button"
              className="metadata-modal__button metadata-modal__button--retry"
              onClick={() => {
                setIsLoading(true);
                setError("");
                getFileMetadata(fileId)
                  .then(setMetadata)
                  .catch((err: unknown) =>
                    setError(
                      err instanceof Error
                        ? err.message
                        : "Error al consultar metadatos.",
                    ),
                  )
                  .finally(() => setIsLoading(false));
              }}
            >
              Reintentar
            </button>
          </div>
        )}

        {!isLoading && metadata && (
          <div className="metadata-modal__body">
            <div className="metadata-grid">
              <div className="metadata-item">
                <span className="metadata-item__label">Nombre del archivo:</span>
                <span className="metadata-item__value">{metadata.fileName}</span>
              </div>

              <div className="metadata-item">
                <span className="metadata-item__label">Tipo / Extensión:</span>
                <span className="metadata-item__value metadata-item__badge">
                  {displayType.toUpperCase()}
                </span>
              </div>

              <div className="metadata-item metadata-item--full">
                <span className="metadata-item__label">Identificador UUID:</span>
                <div className="metadata-item__uuid-wrapper">
                  <code className="metadata-item__uuid">{metadata.id}</code>
                  <button
                    type="button"
                    className="metadata-item__copy-btn"
                    onClick={() => handleCopyUuid(metadata.id)}
                    title="Copiar UUID"
                  >
                    {copied ? "¡Copiado!" : "Copiar"}
                  </button>
                </div>
              </div>

              <div className="metadata-item">
                <span className="metadata-item__label">Tamaño en clúster:</span>
                <span className="metadata-item__value">
                  {formatBytes(metadata.bytesSize)}{" "}
                  <small>({metadata.bytesSize.toLocaleString()} bytes)</small>
                </span>
              </div>

              <div className="metadata-item">
                <span className="metadata-item__label">
                  Particiones (Chunks DFSha):
                </span>
                <span className="metadata-item__value metadata-item__highlight">
                  {metadata.totalPartitions}{" "}
                  {metadata.totalPartitions === 1 ? "chunk" : "chunks"} (R=3)
                </span>
              </div>

              <div className="metadata-item">
                <span className="metadata-item__label">Última modificación:</span>
                <span className="metadata-item__value">
                  {formatDate(new Date(metadata.lastTimeModified))}
                </span>
              </div>

              <div className="metadata-item">
                <span className="metadata-item__label">Carpeta contenedora:</span>
                <span className="metadata-item__value metadata-item__code">
                  {metadata.folderId}
                </span>
              </div>
            </div>
          </div>
        )}

        <div className="metadata-modal__footer">
          <button
            className="metadata-modal__button metadata-modal__button--close"
            type="button"
            onClick={closeMethod}
          >
            Cerrar
          </button>
        </div>
      </div>
    </div>
  );
}

