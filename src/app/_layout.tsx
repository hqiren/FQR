import { Stack } from 'expo-router';
import { ToDoProvider } from '../context/ToDoContext';
import { ScreenTimeProvider } from '../context/ScreenTime';

export default function RootLayout() {
  return (
    <ToDoProvider>
      <ScreenTimeProvider>
        <Stack screenOptions={{ headerShown: false }} />
      </ScreenTimeProvider>
    </ToDoProvider>
  );
}