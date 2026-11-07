import SignUpPage from './pages/SignUpPage'
import {UserPerfileButton} from '../components/users/UserPerfileButton'
import {LogoRow} from '../components/folders/LogoRow'
import {ActionButton} from '../components/folders/ActionButton'
import {File} from '../components/folders/File'
import {DeleteModal} from '../components/folders/DeleteModal'
import {WriteModal} from '../components/folders/WriteModal'
import {FileBlock} from '../components/folders/FolderBlock'




import './App.css'

function App() {

  return (
    <>
      <SignUpPage/>
      <UserPerfileButton/>
      <LogoRow/>
      <File fileName='Prueba' fileType='folder' />
      <File fileName='Prueba1' fileType='jpg' />
      <File fileName='Prueba2' fileType='mp3' />

      <FileBlock folderId='235' folderType='folder' modifiedDate={new Date("2026-10-03T12:00:00Z")} size={123}/>
      
    </>
    
  )
}

export default App

//<WriteModal folderId='163' folderName='prueba folder' closeMethod={()=> console.log("se salio de la funcion")}/>

//<DeleteModal folderId='86' folderName='prueba' closeMethod={()=> console.log("Se borro la carpeta ")}/>