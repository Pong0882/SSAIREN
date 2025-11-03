import { BrowserRouter, Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import PatientListPage from "./pages/PatientListPage";
import { WebSocketProvider } from "./components/providers/WebSocketProvider";

function App() {
  return (
    <WebSocketProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/patientListPage" element={<PatientListPage />} />
        </Routes>
      </BrowserRouter>
    </WebSocketProvider>
  );
}

export default App;
