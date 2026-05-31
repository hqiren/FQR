import { Stack } from 'expo-router';
import { ToDoProvider } from '../context/ToDoContext';

export default function RootLayout() {
  return (
    <ToDoProvider>
      <Stack screenOptions={{ headerShown: false }} />
    </ToDoProvider>
  );
}