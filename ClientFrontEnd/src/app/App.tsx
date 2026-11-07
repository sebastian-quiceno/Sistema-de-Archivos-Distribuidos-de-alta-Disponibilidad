import SignInPage from './pages/SignInPage'
import SignUpPage from './pages/SignUpPage'
import {UserPerfileButton} from '../components/users/UserPerfileButton'
import {LogoRow} from '../components/folders/LogoRow'
import {ActionButton} from '../components/folders/ActionButton'
import {File} from '../components/folders/File'
import {DeleteModal} from '../components/folders/DeleteModal'
import {WriteModal} from '../components/folders/WriteModal'
import {FolderBlock} from '../components/folders/FolderBlock'
import {FileExplorer} from '../components/folders/FileExplorer'


import './App.css'

function App() {

  return (
    <>
      <SignInPage/>
      <UserPerfileButton/>
      <LogoRow/>

      <FileExplorer folderId='0541ed6f-6ee5-4cb2-aa75-e3d4d74d75b1'/>
      

    </>
    
  )
}

export default App
    
//<FolderBlock folderId='235' folderName='PRUEBA' modifiedDate={new Date("2026-10-03T12:00:00Z")}/>



//<FileExplorer folderId=''/>

//<WriteModal folderId='163' folderName='prueba folder' closeMethod={()=> console.log("se salio de la funcion")}/>

//<DeleteModal folderId='86' folderName='prueba' closeMethod={()=> console.log("Se borro la carpeta ")}/>