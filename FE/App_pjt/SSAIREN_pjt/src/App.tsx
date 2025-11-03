//App.tsx
import React from 'react';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import Home from './screens/Home';
import DispatchList from './screens/DispatchList';
import DispatchDetail from './screens/DispatchDetail';

export type RootStackParamList = {
  Home: undefined;
  DispatchList: undefined;
  DispatchDetail: {
    dispatchId: string;
    patientName: string;
    location: string;
    date: string;
  };
};

const Stack = createNativeStackNavigator<RootStackParamList>();

function App() {
  return (
    <SafeAreaProvider>
      <NavigationContainer>
        <Stack.Navigator
          initialRouteName="Home"
          screenOptions={{
            headerShown: false,
            animation: 'slide_from_right',
          }}
        >
          <Stack.Screen name="Home" component={Home} />
          <Stack.Screen name="DispatchList" component={DispatchList} />
          <Stack.Screen name="DispatchDetail" component={DispatchDetail} />
        </Stack.Navigator>
      </NavigationContainer>
    </SafeAreaProvider>
  );
}

export default App;