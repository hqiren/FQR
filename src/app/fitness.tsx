import { MaterialCommunityIcons } from "@expo/vector-icons";
import React from "react";
import { ScrollView, StyleSheet, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import Card from "../components/card";

export default function Fitness() {
  const handleCardPress = (destination: string) => {
    //handle
  };
  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.container}>
        <Text style={styles.title}>Fitness</Text>
        <View style={styles.gridContainer}>
          <Card //steps card
            icon={
              <MaterialCommunityIcons
                name="foot-print"
                size={50}
                color="#e78917"
              />
            }
            title="10,387" //number of steps
            description="Steps"
            onPress={() => handleCardPress("steps")}
          />
          <Card // calories card
            icon={
              <MaterialCommunityIcons name="fire" size={50} color="#e78917" />
            }
            title="3,000" // calories burned
            description="Calories burned"
            onPress={() => handleCardPress("calories")}
          />

          <Card //testing card
            title="oi"
            description="oioi"
          />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "flex-start",
    alignItems: "flex-start",
    backgroundColor: "#f5f5f5",
  },
  title: {
    fontSize: 20,
    fontWeight: "bold",
    marginTop: 30,
    textAlign: "center",
    width: "100%",
  },
  subtitle: {
    fontSize: 14,
    color: "#666",
  },

  gridContainer: {
    flexDirection: "row",
    flexWrap: "wrap",
    justifyContent: "space-evenly",
    width: "100%",
    paddingHorizontal: 16,
  },
});
