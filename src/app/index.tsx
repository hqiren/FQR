import { useScreenTime } from "@/context/ScreenTime";
import { Ionicons } from "@expo/vector-icons";
import { Checkbox } from "expo-checkbox";
import { router } from "expo-router";
import {
  FlatList,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from "react-native";
import { PieChart } from "react-native-gifted-charts";
import { SafeAreaView } from "react-native-safe-area-context";
import { useToDo } from "../context/ToDoContext";

const today = new Date().toLocaleDateString("en-US", {
  weekday: "long",
  year: "numeric",
  month: "long",
  day: "numeric",
});

const palette = [
  "#1877F2",
  "#E1306C",
  "#FF4500",
  "#FFD700",
  "#00C49F",
  "#A855F7",
  "#FF6B6B",
  "#43AA8B",
];

const quotes = [
  "The secret of getting ahead is getting started. – Mark Twain",
  "It always seems impossible until it's done. – Nelson Mandela",
  "Don't watch the clock; do what it does. Keep going. – Sam Levenson",
  "The future depends on what you do today. – Mahatma Gandhi",
  "You don't have to be great to start, but you have to start to be great. – Zig Ziglar",
  "Success is not final, failure is not fatal. – Winston Churchill",
  "Believe you can and you're halfway there. – Theodore Roosevelt",
];

const qotd = quotes[Math.floor(Math.random() * quotes.length)];

type ToDoType = {
  id: number;
  task: string;
  isDone: boolean;
};

type ScreenTimeType = {
  id: number;
  time: number;
  app: string;
};

// Styles
const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: "column",
    justifyContent: "space-between",
  },
  headerText: {
    fontWeight: "bold",
    fontSize: 20,
    fontFamily: "Georgia",
    textAlign: "center",
  },
  date: {
    marginBottom: 5,
    fontFamily: "Georgia",
    textAlign: "center",
    fontSize: 16,
  },
  pieChart: {
    alignItems: "center",
  },
  legend: {
    marginBottom: 10,
  },
  toDoContainer: {
    backgroundColor: "#fff",
    padding: 16,
    borderRadius: 16,
    marginBottom: 10,
  },
  toDoInfoContainer: {
    flexDirection: "row",
    gap: 4,
    alignItems: "center",
  },
  addButton: {
    flexDirection: "row",
    justifyContent: "center",
    alignItems: "center",
    paddingVertical: 8,
  },
  qotd: {
    fontFamily: "Copperplate",
    fontSize: 12,
    textAlign: "center",
  },
  //menuButton: {
  //justifyContent: 'flex-start'
  //}
});

export default function HomeScreen() {
  const { toDoData } = useToDo();
  const { screenTimeData } = useScreenTime();

  const chartData = screenTimeData.map((item, index) => ({
    value: item.time,
    text: item.app,
    color: palette[index % palette.length],
  }));

  return (
    <SafeAreaView style={styles.container}>
      {/* Header Text above Pie Chart */}
      <Text style={styles.headerText}>Today's Screen Time</Text>
      <Text style={styles.date}>{today}</Text>

      {/* Pie Chart */}
      <View style={styles.pieChart}>
        <TouchableOpacity onPress={() => router.push("/screentime")}>
          <View pointerEvents="none">
            <PieChart
              radius={150}
              textSize={20}
              data={chartData}
              showValuesAsLabels
            />
          </View>
        </TouchableOpacity>
      </View>

      {/* Legend */}
      <View style={styles.legend}>
        {chartData.map((item) => (
          <View
            key={item.text}
            style={{ flexDirection: "column", alignItems: "center", gap: 2 }}
          >
            <View style={{ width: 12, height: 12, borderRadius: 6 }} />
            <Text>
              {item.text}: {item.value}h
            </Text>
          </View>
        ))}
      </View>

      {/* Everything below legend */}
      <View style={{ flex: 1, justifyContent: "space-between" }}>
        {/* To Do List */}
        <View>
          <View style={styles.addButton}>
            <Text>Upcoming Activities</Text>
            <TouchableOpacity onPress={() => router.push("/goals")}>
              <Ionicons name="add-circle-outline" size={24} color="black" />
            </TouchableOpacity>
          </View>
          <FlatList
            data={toDoData.slice(0, 3)}
            keyExtractor={(item) => item.id.toString()}
            renderItem={({ item }) => (
              <View style={styles.toDoContainer}>
                <View style={styles.toDoInfoContainer}>
                  <Checkbox value={item.isDone} />
                  <Text
                    style={
                      item.isDone ? { textDecorationLine: "line-through" } : {}
                    }
                  >
                    {item.task}
                  </Text>
                </View>
              </View>
            )}
          />
        </View>

        {/* Quote of the Day */}
        <Text style={styles.qotd}>{qotd}</Text>
      </View>
    </SafeAreaView>
  );
}
