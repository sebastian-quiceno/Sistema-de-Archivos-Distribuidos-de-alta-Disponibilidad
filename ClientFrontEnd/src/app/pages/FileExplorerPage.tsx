import '../../styles/pages/FileExplorerPage.css'
import { useCallback, useState } from "react";

import { LogoRow } from "../../components/folders/LogoRow";
import { AsideButtonsFileExplorer } from "../../components/folders/AsideButtonsFileExplorer";
import { UploadFileModal } from "../../components/folders/UploadFileModal";
import { CreateFolderModal } from "../../components/folders/CreateFolderModal";
import { UserPerfileButton } from "../../components/users/UserPerfileButton";
import { FileExplorer } from "../../components/folders/FileExplorer";
import { useAuth } from "../../hooks/users/useAuth";


export function FileExplorerPage() {
  const { auth } = useAuth();
  const rootFolderId = auth?.rootFolderId?.trim() || null;
  const [currentFolderId, setCurrentFolderId] = useState<string | null>(null);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [showCreateFolderModal, setShowCreateFolderModal] = useState(false);
  const [explorerVersion, setExplorerVersion] = useState(0);
  const [navigateUpToken, setNavigateUpToken] = useState(0);

  const activeFolderId = currentFolderId ?? rootFolderId;

  const handleCurrentFolderChange = useCallback((folderId: string) => {
    setCurrentFolderId(folderId);
  }, []);

  const canNavigateUp = Boolean(
    currentFolderId && rootFolderId && currentFolderId !== rootFolderId,
  );

  return (
    <div className="file-explorer-page">
      <aside className="file-explorer-page__aside">
        <div className="file-explorer-page__aside__logo">
          <LogoRow />
        </div>
        <div className="file-explorer-page__aside__buttons">
          <AsideButtonsFileExplorer
            onCreateFolder={() => {
              if (activeFolderId) {
                setShowCreateFolderModal(true);
              }
            }}
            onUploadFile={() => {
              if (activeFolderId) {
                setShowUploadModal(true);
              }
            }}
            onNavigateUp={() => {
              if (canNavigateUp) {
                setNavigateUpToken((token) => token + 1);
              }
            }}
            navigateUpDisabled={!canNavigateUp}
          />
        </div>
      </aside>
      <main className="file-explorer-page__main">
        <nav>
          <div className="file-explorer-page__main__search">AGREGAR BARRA BUSQUEDA</div>
          <div className="file-explorer-page__main__user">
            <UserPerfileButton />
          </div>
        </nav>
        <div className="file-explorer-page__main__explorer">
          <FileExplorer
            refreshToken={explorerVersion}
            navigateUpToken={navigateUpToken}
            onCurrentFolderChange={handleCurrentFolderChange}
          />
        </div>
      </main>
      {showUploadModal && (
        <UploadFileModal
          folderId={activeFolderId ?? ""}
          closeMethod={() => setShowUploadModal(false)}
          onUploadSuccess={() => setExplorerVersion((version) => version + 1)}
        />
      )}
      {showCreateFolderModal && (
        <CreateFolderModal
          fatherId={activeFolderId ?? ""}
          closeMethod={() => setShowCreateFolderModal(false)}
          onCreateSuccess={() => setExplorerVersion((version) => version + 1)}
        />
      )}
    </div>
  );
}
