import { BrowserRouter, Routes, Route } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import PatientListPage from "./pages/PatientListPage";
import TimeAnalysisPage from "./pages/TimeAnalysisPage";
import PatientStatsPage from "./pages/PatientStatsPage";
import PatientTypePage from "./pages/PatientTypePage";
import ProtectedRoute from "./components/layout/ProtectedRoute";
import { WebSocketProvider } from "./components/providers/WebSocketProvider";

function App() {
  return (
    <WebSocketProvider>
      <BrowserRouter>
        <Routes>
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <PatientListPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/stats/time"
            element={
              <ProtectedRoute>
                <TimeAnalysisPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/stats/patient"
            element={
              <ProtectedRoute>
                <PatientStatsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/stats/type"
            element={
              <ProtectedRoute>
                <PatientTypePage />
              </ProtectedRoute>
            }
          />
          <Route path="/login" element={<LoginPage />} />
        </Routes>
      </BrowserRouter>
    </WebSocketProvider>
  );
}

export default App;
