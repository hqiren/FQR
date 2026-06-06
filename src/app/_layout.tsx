import { MaterialCommunityIcons } from "@expo/vector-icons";
import { Tabs } from "expo-router";
import { ScreenTimeProvider } from "../context/ScreenTime";
import { ToDoProvider } from "../context/ToDoContext";

export default function RootLayout() {
  return (
    <ToDoProvider>
      <ScreenTimeProvider>
        <Tabs
          screenOptions={{
            headerShown: false,
            tabBarActiveTintColor: "#007AFF",
            tabBarInactiveTintColor: "#888888",
          }}
        >
          <Tabs.Screen
            name="index"
            options={{
              title: "Home",
            }}
          />
          <Tabs.Screen
            name="screentime"
            options={{
              title: "Screen Time",
              href: null,
            }}
          />
          <Tabs.Screen
            name="goals"
            options={{
              title: "Goals",
              tabBarIcon: ({ color, size }) => (
                <MaterialCommunityIcons
                  name="target"
                  size={size}
                  color={color}
                />
              ),
            }}
          />
          <Tabs.Screen
            name="fitness"
            options={{
              title: "Fitness",
              tabBarIcon: ({ color, size }) => (
                <MaterialCommunityIcons name="run" size={size} color={color} />
              ),
            }}
          />
        </Tabs>
      </ScreenTimeProvider>
    </ToDoProvider>
  );
}
