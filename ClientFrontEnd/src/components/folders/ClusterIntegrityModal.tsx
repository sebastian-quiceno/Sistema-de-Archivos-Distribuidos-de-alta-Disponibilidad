import { useEffect, useState } from "react";
import "../../styles/folder/ClusterIntegrityModal.css";
import { checkFileIntegrity } from "../../services/fileService";
import type { IntegrityCheckReportType } from "../../types/fileType";

interface ClusterIntegrityModalProps {
  closeMethod: () => void;
}

export function ClusterIntegrityModal({ closeMethod }: ClusterIntegrityModalProps) {
  const [report, setReport] = useState<IntegrityCheckReportType | null>(null);
  const [isChecking, setIsChecking] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    checkFileIntegrity()
      .then((result) => {
        if (active) {
          setReport(result);
          setIsChecking(false);
        }
      })
      .catch((err: unknown) => {
        if (active) {
          setError(
            err instanceof Error
              ? err.message
              : "Error de comunicación al ejecutar la auditoría de integridad.",
          );
          setIsChecking(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  async function runIntegrityCheck() {
    setIsChecking(true);
    setError("");

    try {
      const result = await checkFileIntegrity();
      setReport(result);
    } catch (err: unknown) {
      setError(
        err instanceof Error
          ? err.message
          : "Error de comunicación al ejecutar la auditoría de integridad.",
      );
    } finally {
      setIsChecking(false);
    }
  }

  const isFullyHealthy =
    report && report.corruptedFilesCount === 0 && report.healedFilesCount === 0;
  const isHealed =
    report && report.corruptedFilesCount === 0 && report.healedFilesCount > 0;
  const hasCorrupted = report && report.corruptedFilesCount > 0;

  return (
    <div className="integrity-modal-overlay">
      <div className="integrity-modal-content">
        <div className="integrity-modal__header">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            className="integrity-modal__svg"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10" />
            <path d="m9 12 2 2 4-4" />
          </svg>
          <div className="integrity-modal__titles">
            <h2>Auditoría de Integridad del Clúster</h2>
            <p>DFSha: Verificación y auto-sanación de réplicas distribuidas (R=3)</p>
          </div>
        </div>

        {isChecking && (
          <div className="integrity-modal__loading">
            <div className="integrity-modal__spinner" />
            <p>Auditando chunks y validando firmas en nodos de almacenamiento...</p>
          </div>
        )}

        {error && !isChecking && (
          <div className="integrity-modal__error-box">
            <p className="integrity-modal__error-title">No se pudo completar la auditoría</p>
            <p className="integrity-modal__error-message">{error}</p>
            <button
              type="button"
              className="integrity-modal__btn integrity-modal__btn--retry"
              onClick={runIntegrityCheck}
            >
              Reintentar comprobación
            </button>
          </div>
        )}

        {!isChecking && report && (
          <div className="integrity-modal__body">
            {/* Status Banner */}
            {isFullyHealthy && (
              <div className="integrity-banner integrity-banner--healthy">
                <span className="integrity-banner__badge">100% Íntegro</span>
                <p>
                  Todos los archivos y réplicas coinciden con sus firmas SHA.
                  El clúster está en estado óptimo.
                </p>
              </div>
            )}

            {isHealed && (
              <div className="integrity-banner integrity-banner--healed">
                <span className="integrity-banner__badge">Auto-sanado</span>
                <p>
                  Se detectaron y auto-repararon exitosamente{" "}
                  <strong>{report.healedFilesCount}</strong> réplicas desde nodos sanos.
                </p>
              </div>
            )}

            {hasCorrupted && (
              <div className="integrity-banner integrity-banner--corrupted">
                <span className="integrity-banner__badge">Atención requerida</span>
                <p>
                  Se detectaron <strong>{report.corruptedFilesCount}</strong> archivos
                  con bloques inconsistentes o pérdida de réplicas.
                </p>
              </div>
            )}

            {/* Metrics Grid */}
            <div className="integrity-metrics-grid">
              <div className="integrity-metric-card">
                <span className="integrity-metric-card__value">
                  {report.totalFilesChecked}
                </span>
                <span className="integrity-metric-card__label">Archivos auditados</span>
              </div>

              <div className="integrity-metric-card integrity-metric-card--healthy">
                <span className="integrity-metric-card__value">
                  {report.healthyFilesCount}
                </span>
                <span className="integrity-metric-card__label">Archivos sanos</span>
              </div>

              <div className="integrity-metric-card integrity-metric-card--healed">
                <span className="integrity-metric-card__value">
                  {report.healedFilesCount}
                </span>
                <span className="integrity-metric-card__label">Bloques sanados</span>
              </div>

              <div className="integrity-metric-card integrity-metric-card--corrupted">
                <span className="integrity-metric-card__value">
                  {report.corruptedFilesCount}
                </span>
                <span className="integrity-metric-card__label">Archivos corruptos</span>
              </div>
            </div>

            {/* Issues Section */}
            <div className="integrity-issues-section">
              <h3>Incidencias detectadas</h3>
              {report.issues && report.issues.length > 0 ? (
                <div className="integrity-issues-list">
                  {report.issues.map((issue) => (
                    <div key={issue.fileId} className="integrity-issue-item">
                      <div className="integrity-issue-item__header">
                        <span className="integrity-issue-item__name">
                          {issue.fileName}
                        </span>
                        <code className="integrity-issue-item__id">
                          {issue.fileId}
                        </code>
                      </div>
                      <p className="integrity-issue-item__msg">
                        {issue.errorMessage}
                      </p>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="integrity-issues-empty">
                  ✓ No se encontraron problemas ni pérdidas de réplicas en los archivos auditados.
                </p>
              )}
            </div>
          </div>
        )}

        <div className="integrity-modal__footer">
          <button
            className="integrity-modal__btn integrity-modal__btn--secondary"
            type="button"
            onClick={closeMethod}
            disabled={isChecking}
          >
            Cerrar
          </button>
          <button
            className="integrity-modal__btn integrity-modal__btn--primary"
            type="button"
            onClick={runIntegrityCheck}
            disabled={isChecking}
          >
            {isChecking ? "Analizando..." : "Re-analizar clúster"}
          </button>
        </div>
      </div>
    </div>
  );
}