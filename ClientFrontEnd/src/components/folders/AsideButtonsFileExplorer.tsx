import type { SVGProps } from "react";

import { ButtonWithIcon } from "./ButtonWithIcon";
import "../../styles/folder/AsideButtonsFileExplorer.css";

interface AsideButtonsFileExplorerProps {
  onCreateFolder?: () => void;
  onUploadFile?: () => void;
  onNavigateUp?: () => void;
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

function MoreIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg {...props} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
      <circle cx="12" cy="12" r="1" />
      <circle cx="19" cy="12" r="1" />
      <circle cx="5" cy="12" r="1" />
    </svg>
  );
}

export function AsideButtonsFileExplorer({
  onCreateFolder,
  onUploadFile,
  onNavigateUp,
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
      <ButtonWithIcon
        Icono={MoreIcon}
        background="#A48370"
        color="#EDD9CB"
        colorIco="#EDD9CB"
        text="Ver mas"
        onClick={onOpenMore}
      />
    </div>
  );
}
