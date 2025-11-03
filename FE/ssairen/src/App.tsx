function App() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
      <div className="max-w-2xl w-full bg-white rounded-2xl shadow-2xl p-8">
        <h1 className="text-4xl font-bold text-gray-800 mb-4 text-center">
          SSAIREN 🏥
        </h1>
        <p className="text-lg text-gray-600 mb-8 text-center">
          Hospital Management System
        </p>

        <div className="space-y-4">
          <div className="bg-blue-100 border-l-4 border-blue-500 p-4 rounded">
            <p className="text-blue-700 font-semibold">✅ React 19 - Working!</p>
          </div>

          <div className="bg-green-100 border-l-4 border-green-500 p-4 rounded">
            <p className="text-green-700 font-semibold">✅ TypeScript - Working!</p>
          </div>

          <div className="bg-purple-100 border-l-4 border-purple-500 p-4 rounded">
            <p className="text-purple-700 font-semibold">✅ Vite - Working!</p>
          </div>

          <div className="bg-pink-100 border-l-4 border-pink-500 p-4 rounded">
            <p className="text-pink-700 font-semibold">✅ Tailwind CSS - Working!</p>
          </div>
        </div>

        <div className="mt-8 flex gap-4 justify-center">
          <button className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-3 px-6 rounded-lg transition-colors duration-200 shadow-lg hover:shadow-xl">
            Get Started
          </button>
          <button className="bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-3 px-6 rounded-lg transition-colors duration-200">
            Learn More
          </button>
        </div>

        <div className="mt-8 p-4 bg-gray-50 rounded-lg">
          <p className="text-sm text-gray-500 text-center">
            Stack: React + TypeScript + Vite + Tailwind CSS v4
          </p>
        </div>
      </div>
    </div>
  )
}

export default App
