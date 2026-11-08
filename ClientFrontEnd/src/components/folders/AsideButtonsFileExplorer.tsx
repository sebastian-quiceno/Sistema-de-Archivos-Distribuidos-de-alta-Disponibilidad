import type { SVGProps } from "react";

import { ButtonWithIcon } from "./ButtonWithIcon";
import "../../styles/folder/AsideButtonsFileExplorer.css";

interface AsideButtonsFileExplorerProps {
  onCreateFolder?: () => void;
  onUploadFile?: () => void;
  onNavigateUp?: () => void;
  onCheckIntegrity?: () => void;
  onOpenMore?: () => void;
  navigateUpDisabled?: boolean;
}

function NewFolderIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg {...props} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
      <path d="M20 20a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.9a2 2 0 0 1-1.69-.9L9.6 3.9A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13a2 2 0 0 0 2 2Z" />
      <path d="M12 10v6" />
      <path d="M9 13h6" />
    </svg>
  );
}

function UploadIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg {...props} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
      <path d="M5 12h14" />
      <path d="M12 5v14" />
    </svg>
  );
}

function NavigateUpIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg {...props} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
      <path d="M9 14 4 9l5-5" />
      <path d="M4 9h10.5a5.5 5.5 0 0 1 5.5 5.5a5.5 5.5 0 0 1-5.5 5.5H11" />
    </svg>
  );
}

function ShieldCheckIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg {...props} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10" />
      <path d="m9 12 2 2 4-4" />
    </svg>
  );
}

export function AsideButtonsFileExplorer({
  onCreateFolder,
  onUploadFile,
  onNavigateUp,
  onCheckIntegrity,
  onOpenMore,
  navigateUpDisabled = false,
}: AsideButtonsFileExplorerProps) {
  return (
    <div className="file-explorer-actions">
      <ButtonWithIcon
        Icono={NewFolderIcon}
        background="#F0C785"
        color="#4B3B22"
        colorIco="#4B3B22"
        text="Nueva Carpeta"
        onClick={onCreateFolder}
      />
      <ButtonWithIcon
        Icono={UploadIcon}
        background="#FFFFFF"
        color="#66687A"
        colorIco="#66687A"
        text="Subir Archivo"
        onClick={onUploadFile}
      />
      <ButtonWithIcon
        Icono={NavigateUpIcon}
        background="#80BCE2"
        color="#4D6A7D"
        colorIco="#4D6A7D"
        text="Subir de carpeta"
        onClick={onNavigateUp}
        disabled={navigateUpDisabled}
      />
      {(onCheckIntegrity || onOpenMore) && (
        <ButtonWithIcon
          Icono={ShieldCheckIcon}
          background="#A48370"
          color="#EDD9CB"
          colorIco="#EDD9CB"
          text="Estado Clúster"
          onClick={onCheckIntegrity ?? onOpenMore}
        />
      )}
    </div>
  );
}
